package com.helloworld.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudCredentials;


public class HomeFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static Context mContext;
    private org.apache.commons.httpclient.methods.GetMethod getMethod;
    private OwnCloudBasicCredentials cred;
    private OwnCloudCredentials c;
    public OwnCloudClient mClient;
    private Bitmap map;
    private ImageView img;
    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container , false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        Button logoutBtn = (Button) v.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity() , Authentication.class);
                startActivity(intent);
            }
        });
        return v;
    }
}
