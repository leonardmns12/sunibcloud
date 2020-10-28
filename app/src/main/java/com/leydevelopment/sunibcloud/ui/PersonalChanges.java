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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.leydevelopment.sunibcloud.R;
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

public class PersonalChanges extends AppCompatActivity implements OnRemoteOperationListener {
    private EditText password;
    private Button connect;
    private Handler mHandler = new Handler();
    private OwnCloudClient mClient;

    private String uname , phoneNumb;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_changes);
        getSupportActionBar().hide();
        password    = findViewById(R.id.password);
        connect     = findViewById(R.id.connect);
        mAuth       = FirebaseAuth.getInstance();
        phoneNumb   = mAuth.getCurrentUser().getPhoneNumber();
        mContext    = this;

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readUsername();
            }
        });
    }

    private void readUsername() {
        db.document("users/" + phoneNumb).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                uname = documentSnapshot.get("username").toString();
                connectAccount(uname);
            }
        });
    }

    private void connectAccount(String username) {
        Uri serverUri = Uri.parse("https://indofolks.com");
        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri , this, false);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials( username , password.getText().toString()));
        GetUserInfoRemoteOperation getUser = new GetUserInfoRemoteOperation();
        getUser.execute(mClient, this , mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) throws IOException {
        if (result.getHttpCode() == 401) {
            Toast.makeText(this , "Invalid Password!" , Toast.LENGTH_SHORT).show();
        } else if (result.getHttpCode() == 200) {
            Map<String, Object> user = new HashMap<>();
            user.put("password" , password.getText().toString());
            db.document("users/" +phoneNumb).update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent intent = new Intent(mContext , SuccessLogin.class);
                    intent.putExtra("SUCCESS_ID" , "personalChanges");
                    startActivity(intent);
                }
            });

        }

    }
}