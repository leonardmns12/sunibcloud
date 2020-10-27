package com.leydevelopment.sunibcloud.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.leydevelopment.sunibcloud.R;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConnectAccount extends AppCompatActivity implements OnRemoteOperationListener {

    private EditText username , password;
    private Button connectBtn;
    private OwnCloudClient mClient;
    private Handler mHandler;
    private Context mContext;
    private OwnCloudBasicCredentials cred;
    private FirebaseFirestore db;
    private String phoneNumb;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_account);
        getSupportActionBar().hide();
        db              = FirebaseFirestore.getInstance();
        username        = findViewById(R.id.username);
        password        = findViewById(R.id.password);
        connectBtn      = findViewById(R.id.connectBtn);
        mHandler        = new Handler();
        mContext        = this;
        mAuth           = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        phoneNumb       = currentUser.getPhoneNumber();
        Uri serverUri   = Uri.parse("https://indofolks.com");
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
                mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username.getText().toString(), password.getText().toString()));
                UserInfo();
            }
        });
    }

    private void UserInfo() {
        GetUserInfoRemoteOperation uInfo = new GetUserInfoRemoteOperation();
        uInfo.execute(mClient, this , mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) throws IOException {
        if(caller instanceof GetUserInfoRemoteOperation){
            onSuccessfulGetUserInfo(caller ,result);
        }
    }

    private void onSuccessfulGetUserInfo(RemoteOperation caller, RemoteOperationResult result) {
        if (result.getHttpCode() == 200){
            connectAccount();
        } else if (result.getHttpCode() == 401){
            Toast.makeText(this , "Invalid username/password!" , Toast.LENGTH_SHORT).show();
        }
    }

    private void connectAccount() {
        addNewUsers();
    }

    private void addNewUsers() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username.getText().toString());
        user.put("password", password.getText().toString());
        db.document("users/"+phoneNumb).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(ConnectAccount.this , SuccessLogin.class);
                intent.putExtra("SUCCESS_ID" , "connect");
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ConnectAccount.this , "Failed to connect to server!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConnectAccount.this , Login.class);
                startActivity(intent);
            }
        });
    }
}