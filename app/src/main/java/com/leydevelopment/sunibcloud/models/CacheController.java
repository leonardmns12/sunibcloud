package com.leydevelopment.sunibcloud.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.leydevelopment.sunibcloud.BuildConfig;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.os.Environment.isExternalStorageRemovable;

public class CacheController {

    //disk cache
    private DiskLruCache diskLruCache;
    private final Object diskCacheLock = new Object();
    private boolean diskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";


    private Context mContext;

    public CacheController(Context mContext) {
        this.mContext = mContext;
    }

    public void addCache() {
        InitDiskCacheTask init = new InitDiskCacheTask();
        File cacheDir = getDiskCacheDir(mContext, DISK_CACHE_SUBDIR);
        init.execute(cacheDir);
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


}
