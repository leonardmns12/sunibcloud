package com.leydevelopment.sunibcloud;

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

    //disk cache
    private DiskLruCache diskLruCache;
    private final Object diskCacheLock = new Object();
    private boolean diskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

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

        //cache
        File cacheDir = getDiskCacheDir(mContext, DISK_CACHE_SUBDIR);
        new InitDiskCacheTask().execute(cacheDir);
        //

        if ( getItem(position).getRemotePath().endsWith("/")) {
            folder.setVisibility(View.VISIBLE);
            folder.setImageResource(R.drawable.ic_baseline_folder_24);
//            thumbnail.setVisibility(View.GONE);
            return convertView;
        } else if ( getItem(position).getRemotePath().endsWith("png") || getItem(position).getRemotePath().endsWith("jpg") || getItem(position).getRemotePath().endsWith("jpeg")) {
            String key = getItem(res).getRemotePath();
                Bitmap bp = getBitmapFromDiskCache(getItem(res).getRemotePath());

                if(bp == null) {
                    FilesArrayAdapter.ThumbnailGenerationTask task = new ThumbnailGenerationTask();
                    try {
                        bp = task.execute().get();
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

    class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            synchronized (diskCacheLock) {
                File cacheDir = params[0];
                try {
                    diskLruCache = DiskLruCache.open(cacheDir, 1 , 1 , DISK_CACHE_SIZE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                diskCacheStarting = false; // Finished initialization
                diskCacheLock.notifyAll(); // Wake any waiting threads
            }
            return null;
        }
    }

    public class ThumbnailGenerationTask extends AsyncTask<ThumbnailGenerationTask, Void , Bitmap> {
        @Override
        protected void onPreExecute() {
            return;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
        }

        @SuppressLint("WrongThread")
        @Override
        protected Bitmap doInBackground(ThumbnailGenerationTask... thumbnailGenerationTasks) {
            Bitmap thumbnail = null;
            thumbnail = GenerateThumbnail();
            thumbnail = getRoundedCornerBitmap(thumbnail,20);
            addBitmapToCache(getItem(res).getRemotePath(), thumbnail);
//            folder.setImageBitmap(thumbnail);
            return thumbnail;
        }
    }

    private Bitmap GenerateThumbnail() {
        Bitmap thumbnail = null;
        try {
            Uri serverUri = Uri.parse("https://indofolks.com");
            String filePath = getItem(res).getRemotePath();
            if (filePath.contains(" ")) {
                filePath = filePath.replaceAll("\\s" , "%20");
            }
            String uriPhoto = "https://indofolks.com/index.php/apps/files/api/v1/thumbnail/150/150/" + filePath;
            cred = new OwnCloudBasicCredentials("leonard" , "gurame442");
            OwnCloudAccount ocAccount = new OwnCloudAccount(serverUri , cred);
            mClient = OwnCloudClientManagerFactory.getDefaultSingleton().getClientFor(ocAccount , getContext());
            getMethod = new GetMethod(uriPhoto);
            getMethod.setRequestHeader("Cookie",
                    "nc_sameSiteCookielax=true;nc_sameSiteCookiestrict=true");

            getMethod.setRequestHeader(RemoteOperation.OCS_API_HEADER,
                    RemoteOperation.OCS_API_HEADER_VALUE);
            Log_OC.d("TAG", "generate thumbnail: " + "capture.png" + " URI: " + uriPhoto);
            int status = mClient.executeMethod(getMethod);
            if (status == HttpStatus.SC_OK) {
                Log.e("TEST" , "FINISH");
                InputStream inputStream = getMethod.getResponseBodyAsStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 150, 150);
            } else {
                mClient.exhaustResponse(getMethod.getResponseBodyAsStream());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }
        return thumbnail;
    }

    private Bitmap handlePNG(Bitmap bitmap, int pxW, int pxH) {
        Bitmap resultBitmap = Bitmap.createBitmap(pxW, pxH, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(resultBitmap);
        // TODO check based on https://github.com/nextcloud/android/pull/3459#discussion_r339935975
        c.drawColor(mContext.getResources().getColor(R.color.red));
        c.drawBitmap(bitmap, 0, 0, null);
        return resultBitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ?
                        context.getCacheDir().getPath() : null ;
        return new File(cachePath + File.separator + uniqueName);
    }

    public void addBitmapToCache(String key, Bitmap bitmap) {
        synchronized (diskCacheLock) {
            if (diskLruCache != null && get(key) == null) {
                put(key, bitmap);
            }
        }
    }

    public Bitmap getBitmapFromDiskCache(String key) {
        synchronized (diskCacheLock) {
            // Wait while disk cache is started from background thread
            while (diskCacheStarting) {
                try {
                    diskCacheLock.wait();
                } catch (InterruptedException e) {}
            }
            if (diskLruCache != null) {
                return get(key);
            }
        }
        return null;
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), 8 * 1024);
            return bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void put(String key, Bitmap bitmap) {
        DiskLruCache.Editor editor = null;
        String validKey = convertToValidKey(key);
        try {
            editor = diskLruCache.edit(validKey);
            if (editor == null) {
                return;
            }
            if (writeBitmapToFile(bitmap, editor)) {
                diskLruCache.flush();
                editor.commit();
                if (BuildConfig.DEBUG) {
                    Log_OC.d("CACHE_TEST_DISK", "image put on disk cache " + validKey);
                }
            } else {
                editor.abort();
                if (BuildConfig.DEBUG) {
                    Log_OC.d("CACHE_TEST_DISK", "ERROR on: image put on disk cache " + validKey);
                }
            }
        } catch (Exception e){
            Log.e("ERR" , e.getMessage());
        }
    }

    public Bitmap get(String key) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        String validKey = convertToValidKey(key);
        try {
            snapshot = diskLruCache.get(validKey);
            if (snapshot == null) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream(in, 8 * 1024);
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (IOException e){
            Log.e("Err" , e.getMessage());
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }
        return bitmap;
    }

    private String convertToValidKey(String key) {
        return Integer.toString(key.hashCode());
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
