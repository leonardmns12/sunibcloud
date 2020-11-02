package com.leydevelopment.sunibcloud.models;

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
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class ThumbnailManager {

    private OwnCloudClient mClient;
    private GetMethod getMethod;
    private String path , cachePath;
    private String MODE_THUMBNAILS;
    private Context mContext;
    private CacheController cc;

    public ThumbnailManager(OwnCloudClient mClient, String path, String MODE_THUMBNAILS , Context mContext) {
        this.mClient = mClient;
        this.path = path;
        this.MODE_THUMBNAILS = MODE_THUMBNAILS;
        this.mContext = mContext;
        cc = new CacheController(mContext);
        cc.addCache();
        cachePath = path;
    }

    public Bitmap ThumbnailTask() throws ExecutionException, InterruptedException {
        ThumbnailGenerationTask task = new ThumbnailGenerationTask();
        return task.execute().get();
    }

    public class ThumbnailGenerationTask extends AsyncTask<ThumbnailManager.ThumbnailGenerationTask, Void , Bitmap> {
        @Override
        protected void onPreExecute() {
            return;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
        }

        @SuppressLint("WrongThread")
        @Override
        protected Bitmap doInBackground(ThumbnailManager.ThumbnailGenerationTask... thumbnailGenerationTasks) {
            Bitmap thumbnail = null;
            thumbnail = GenerateThumbnail();
            if(MODE_THUMBNAILS.equals("api")){
                thumbnail = getRoundedCornerBitmap(thumbnail, 20);
                cc.addBitmapToCache(cachePath , thumbnail);
            }else {
                cc.addBitmapToCache("preview_" +path , thumbnail);
            }
            return thumbnail;
        }
    }

    private Bitmap GenerateThumbnail() {
        Bitmap thumbnail = null;
        String uriPhoto;
        try {
            Uri serverUri = Uri.parse("https://indofolks.com");
            if (path.contains(" ")) {
                path = path.replaceAll("\\s" , "%20");
            }
            if (MODE_THUMBNAILS.equals("preview")){
                uriPhoto = "https://indofolks.com/index.php/core/preview.png?file=" + path + "&x=" + 500 + "&y=" + 500 + "&a=1&mode=cover&forceIcon=0";
            }else{
                uriPhoto = "https://indofolks.com/index.php/apps/files/api/v1/thumbnail/150/150/" + path;
            }
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
                thumbnail = BitmapFactory.decodeStream(inputStream);
                if(MODE_THUMBNAILS.equals("api")){
                    thumbnail = ThumbnailUtils.extractThumbnail(thumbnail, 150, 150);
                }
            } else {
                mClient.exhaustResponse(getMethod.getResponseBodyAsStream());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnail;
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
}
