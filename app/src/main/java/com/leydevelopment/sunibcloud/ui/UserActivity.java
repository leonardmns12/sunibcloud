package com.leydevelopment.sunibcloud.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.leydevelopment.sunibcloud.MainActivity;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.adapter.ActivityAdapter;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.activities.GetActivitiesRemoteOperation;
import com.owncloud.android.lib.resources.activities.model.Activity;

import java.io.ObjectInputStream;
import java.util.ArrayList;

public class UserActivity extends AppCompatActivity implements OnRemoteOperationListener {

    private OwnCloudClient client;

    private RecyclerView activityList;

    private Handler mHandler;
    private Context mContext;

    private ProgressBar progressBar;

    private String ACTIVITY_KEY = "activitykey";

    private ConstraintLayout userLayout;

    private transient ObjectInputStream ois;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        getSupportActionBar().hide();
        userLayout = (ConstraintLayout) findViewById(R.id.userLayout);
        userLayout.setBackgroundColor(Color.parseColor("#29A9DF"));
        mContext = this;
        mHandler = new Handler();
        progressBar = (ProgressBar) findViewById(R.id.spin_kit);
        Sprite threeBounce = new ThreeBounce();
        threeBounce.setColor(Color.WHITE);
        progressBar.setIndeterminateDrawable(threeBounce);
        activityList = (RecyclerView) findViewById(R.id.activityList);
        ArrayList<Activity> activities1 = null;
        ActivityAdapter activityAdapter = new ActivityAdapter(mContext , activities1);
        activityList.setAdapter(activityAdapter);
        Uri serverUri = Uri.parse("http://indofolks.com/");
        client = OwnCloudClientFactory.createOwnCloudClient(serverUri , this , true);
        client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials("leonard", "gurame442"));
        getActivities();
    }

    private void getActivities() {
        GetActivitiesRemoteOperation getActivities = new GetActivitiesRemoteOperation();
        getActivities.execute(client, this , mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        if (caller instanceof GetActivitiesRemoteOperation) {
            onSuccesfulyGetActivities(caller, result);
        }
    }

    private void onSuccesfulyGetActivities(RemoteOperation caller, RemoteOperationResult result) {
        progressBar.setVisibility(View.GONE);
        userLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        ArrayList<Activity> activities = (ArrayList<Activity>) result.getData().get(0);
        ActivityAdapter activityAdapter = new ActivityAdapter(mContext , activities);
        activityList.setAdapter(activityAdapter);
        activityList.setLayoutManager(new LinearLayoutManager(this));
        Log.e("Test" , "masuk");
    }

}