package com.leydevelopment.sunibcloud.ui;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.models.ThumbnailManager;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;

import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class PreviewImage extends AppCompatActivity {

    PhotoView photoView;
    private String path;
    private OwnCloudBasicCredentials cred;
    private OwnCloudClient mClient;
    private GetMethod getMethod;
    private Bitmap bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        path            = getIntent().getExtras().getString("path");
        photoView       = findViewById(R.id.photoView);
        Uri serverUri = Uri.parse("https://indofolks.com");
        cred = new OwnCloudBasicCredentials("leonard" , "gurame442");
        OwnCloudAccount ocAccount = new OwnCloudAccount(serverUri , cred);
        try {
            mClient = OwnCloudClientManagerFactory.getDefaultSingleton().getClientFor(ocAccount , this);
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ThumbnailManager tm = new ThumbnailManager(mClient , path, "preview" , this);
        try {
            bp = tm.ThumbnailTask();
            photoView.setImageBitmap(bp);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}