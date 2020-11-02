package com.leydevelopment.sunibcloud.adapter;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.collection.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;
import com.leydevelopment.sunibcloud.BuildConfig;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.models.CacheController;
import com.leydevelopment.sunibcloud.models.ThumbnailManager;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.model.RemoteFile;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.Cache;

import static android.os.Environment.isExternalStorageRemovable;

public class FilesArrayAdapter extends ArrayAdapter<RemoteFile> {
    private List<String> items ;
    private String uri;
    private Context mContext;
    public OwnCloudClient mClient;
    private ImageView thumbnail;
    private org.apache.commons.httpclient.methods.GetMethod getMethod;
    private OwnCloudBasicCredentials cred;
    private int res;
    private ViewHolder holder;
    private ImageView folder;
    private LruCache<String, Bitmap> memoryCache;
    private CacheController cc;
    public static class ViewHolder {
//        TextView textView;
//        ImageView folder;
    }
    public FilesArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        res = position;
        holder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_in_list, parent, false);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
        mContext = getContext();
        TextView Filename = (TextView)convertView.findViewById(R.id.fileName);
        TextView textView = (TextView)convertView.findViewById(R.id.viewId);
        Filename.setText(getFileName(getItem(position).getRemotePath()));
        textView.setText(getItem(position).getRemotePath());
        folder = (ImageView) convertView.findViewById(R.id.folderImg);
        Uri serverUri = Uri.parse("https://indofolks.com");
        cred = new OwnCloudBasicCredentials("leonard" , "gurame442");
        OwnCloudAccount ocAccount = new OwnCloudAccount(serverUri , cred);
        try {
            mClient = OwnCloudClientManagerFactory.getDefaultSingleton().getClientFor(ocAccount , getContext());
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //cache
//        File cacheDir = getDiskCacheDir(mContext, DISK_CACHE_SUBDIR);
//        new InitDiskCacheTask().execute(cacheDir);
        cc = new CacheController(getContext());
        cc.addCache();
        //

        if ( getItem(position).getRemotePath().endsWith("/")) {
            folder.setVisibility(View.VISIBLE);
            folder.setImageResource(R.drawable.ic_baseline_folder_24);
//            thumbnail.setVisibility(View.GONE);
            return convertView;
        } else if ( getItem(position).getRemotePath().endsWith("png") || getItem(position).getRemotePath().endsWith("jpg") || getItem(position).getRemotePath().endsWith("jpeg")) {
            String key = getItem(res).getRemotePath();
                Bitmap bp = cc.getBitmapFromDiskCache(getItem(res).getRemotePath());
                if(bp == null) {
                    ThumbnailManager tm = new ThumbnailManager(mClient , getItem(res).getRemotePath(), "api" , getContext());
                    try {
                        bp = tm.ThumbnailTask();
                        folder.setImageBitmap(bp);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    folder.setImageBitmap(bp);
                }
        } else if (getItem(position).getRemotePath().endsWith("mp4") || getItem(position).getRemotePath().endsWith("mov")){
            folder.setImageResource(R.drawable.ic_baseline_movie_creation_24);
        }
        else if (getItem(position).getRemotePath().endsWith("apk")){
            folder.setImageResource(R.drawable.ic_baseline_android_24);
        }
        else if (getItem(position).getRemotePath().endsWith("pdf")) {
            folder.setImageResource(R.drawable.ic_baseline_picture_as_pdf_24);
        } else if (getItem(position).getRemotePath().endsWith("docx")) {
            folder.setImageResource(R.drawable.ic_baseline_assignment_24);
        }else if (getItem(position).getRemotePath().endsWith("cpp") || getItem(position).getRemotePath().endsWith("java") || getItem(position).getRemotePath().endsWith("c") || getItem(position).getRemotePath().endsWith("js")) {
            folder.setImageResource(R.drawable.ic_baseline_code_24);
        }else {
            folder.setImageResource(R.drawable.ic_baseline_folder_24);
        }

        return convertView;
    }
    private String getFileName(String paths) {
        String Filename = "";
        int idx = 0;

        for (int i = paths.length()-1; i >= 0; i--) {
            if (paths.charAt(i) == '/') {
                if (i == paths.length()-1){
                    continue;
                }else{
                    idx = i+1;
                    break;
                }
            }
        }
        Filename = paths.substring(idx , paths.length());

        if (Filename.endsWith("/")){
            Filename = Filename.substring(0 , Filename.length()-1);
        }

        return Filename;
    }
}
