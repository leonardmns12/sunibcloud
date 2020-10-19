package com.leydevelopment.sunibcloud;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation;

import java.util.Calendar;
import java.util.Objects;


public class HomeFragment extends Fragment implements OnRemoteOperationListener {
    private FirebaseAuth mAuth;
    private static Context mContext;
    private org.apache.commons.httpclient.methods.GetMethod getMethod;
    private OwnCloudBasicCredentials cred;
    private OwnCloudCredentials c;
    public OwnCloudClient mClient;
    private Bitmap map;
    private ImageView img;

    private ImageView iconMsg;

    private TextView quotaUsed , totalQuota , greetingMsg;

    private CardView homeUser , trashBin;

    private Handler mHandler;
    private float usedQuota , freeQuota , quota;

    public final String USED_QUOTA_KEY = "used_quota";
    public final String TOTAL_QUOTA_KEY = "total_quota";

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container , false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        Button logoutBtn = (Button) v.findViewById(R.id.logoutBtn);
        quotaUsed = (TextView) v.findViewById(R.id.quotaUsed);
        totalQuota = (TextView) v.findViewById(R.id.totalquota);
        greetingMsg = (TextView) v.findViewById(R.id.greetingMsg);
        iconMsg = (ImageView) v.findViewById(R.id.iconMsg);
        homeUser = (CardView) v.findViewById(R.id.homeUser);
        trashBin = (CardView) v.findViewById(R.id.trashBin);
        getGreetingMsg();
        readData(TOTAL_QUOTA_KEY);
        readData(USED_QUOTA_KEY);
        Uri serverUri = Uri.parse("https://indofolks.com");
        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, getActivity(), true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials("leonard", "gurame442"));
        mHandler = new Handler();
        userInfo();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity() , Authentication.class);
                startActivity(intent);
            }
        });
        homeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity() , UserActivity.class);
                startActivity(intent);
            }
        });
        trashBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity() , TrashbinActivity.class);
                startActivity(intent);
            }
        });
        return v;
    }

    @SuppressLint("SetTextI18n")
    private void getGreetingMsg() {
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        if (currentHour > 6 && currentHour <= 12) {
            greetingMsg.setText("Good Morning,");
            iconMsg.setImageResource(R.drawable.ic_baseline_wb_sunny_24);
        } else if (currentHour >= 13 && currentHour <= 15){
            greetingMsg.setText("Good Afternoon,");
            iconMsg.setImageResource(R.drawable.ic_baseline_cloud_24);
        } else if (currentHour > 15 && currentHour <= 18) {
            greetingMsg.setText("Good Evening,");
            iconMsg.setImageResource(R.drawable.ic_baseline_nights_stay_24);
        } else {
            greetingMsg.setText("Good Night,");
            iconMsg.setImageResource(R.drawable.ic_baseline_brightness_2_24);
        }
    }

    private void userInfo() {
        GetUserInfoRemoteOperation userinfo = new GetUserInfoRemoteOperation();
        userinfo.execute(mClient , this, mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
        if (caller instanceof GetUserInfoRemoteOperation) {
            onSuccessfulGetInfo(caller, result);
        }
    }

    private void onSuccessfulGetInfo(RemoteOperation caller, RemoteOperationResult result) {
        UserInfo userInfo = (UserInfo) result.getData().get(0);
        usedQuota = userInfo.quota.used;
        quota = userInfo.quota.quota;
        quotaUsed.setText(getQuotaUsed(usedQuota , "used"));
        totalQuota.setText(getQuotaUsed(quota , "total"));
        saveData(quotaUsed.getText().toString() , USED_QUOTA_KEY);
        saveData(totalQuota.getText().toString() , TOTAL_QUOTA_KEY);
    }

    private void saveData(String data , String key) {
        try{
            SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(key, data);
            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void readData(String key) {
        try{
            SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
            String value = sharedPref.getString(key, "");
            if (key.equals(USED_QUOTA_KEY)) {
                quotaUsed.setText(value);
            } else if (key.equals(TOTAL_QUOTA_KEY)) {
                totalQuota.setText(value);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @SuppressLint("DefaultLocale")
    public String getQuotaUsed(float val, String type) {
        if (val < 1000000) {
            val = val / 100000;
            if(type.equals("used")){
                return String.format("%.2f",val) +" KB";
            }else {
                return "/ " + String.format("%.2f",val) +" KB";
            }
        } else if(val > 1000000 && val < 1000000000) {
            val = val / 1000000;
            if(type.equals("used")){
                return String.format("%.2f",val) +" MB";
            }else {
                return "/ " + String.format("%.2f",val) +" MB";
            }
        } else if(val > 1000000000) {
            val = val/1000000000;
            if(type.equals("used")){
                return String.format("%.2f",val) +" GB";
            }else {
                return "/ " + String.format("%.0f",val) +" GB";
            }
        }
        return "";
    }
}


