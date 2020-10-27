package com.leydevelopment.sunibcloud.ui;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.leydevelopment.sunibcloud.R;
import com.leydevelopment.sunibcloud.models.CacheController;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudBasicCredentials;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.UserInfo;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.users.GetUserAvatarRemoteOperation;
import com.owncloud.android.lib.resources.users.GetUserInfoRemoteOperation;

import java.io.IOException;
import java.util.ArrayList;


public class SettingFragment extends Fragment implements OnRemoteOperationListener {

    private OwnCloudClient mClient;
    private Handler mHandler;
    private ImageView avatar;
    private OwnCloudBasicCredentials cred;
    private OwnCloudAccount ocAccount;
    private TextView displayName;
    private EditText email , address;
    private Button saveBtn;
    private CacheController cc;

    private Bitmap cacheProfile;
    private FirebaseAuth mAuth;

    private String uAddress;

    enum Field {
        EMAIL("email"),
        DISPLAYNAME("displayname"),
        PHONE("phone"),
        ADDRESS("address"),
        WEBSITE("website"),
        TWITTER("twitter");

        private final String fieldName;

        Field(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
        private Field field;

    }

    @Nullable
    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_setting, container , false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        avatar      = (ImageView) v.findViewById(R.id.avatar);
        displayName = (TextView) v.findViewById(R.id.displayName);
        email       = (EditText) v.findViewById(R.id.email);
        address     = (EditText) v.findViewById(R.id.address);
        saveBtn     = (Button) v.findViewById(R.id.confirmSave);
        mHandler = new Handler();
        mAuth = FirebaseAuth.getInstance();
        Uri serverUri = Uri.parse("https://indofolks.com");
        cred = new OwnCloudBasicCredentials("leonard" , "gurame442");
        ocAccount = new OwnCloudAccount(serverUri , cred);
        try {
            mClient = OwnCloudClientManagerFactory.getDefaultSingleton().getClientFor(ocAccount , getContext());
        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
            }
        });
        cc = new CacheController(getContext());
        cc.addCache();
        cacheProfile = cc.getBitmapFromDiskCache("avatar");
        avatar.setImageBitmap(cacheProfile);

        getRemoteAvatar();
        getUserInfo();
        return v;
    }

    private void saveInfo() {
//        SetUserInfoRemoteOperation saveInfo = new SetUserInfoRemoteOperation("email" , "leonardmanoza@gmail.com" );
//        saveInfo.execute(mClient, this , mHandler);
        mAuth.signOut();
        Intent intent = new Intent(getActivity() , Authentication.class);
        startActivity(intent);
    }

    private void getRemoteAvatar() {
        GetUserAvatarRemoteOperation getAvatar = new GetUserAvatarRemoteOperation(300 , null);
        getAvatar.execute(mClient , this , mHandler);
    }

    private void getUserInfo() {
        GetUserInfoRemoteOperation getUser = new GetUserInfoRemoteOperation();
        getUser.execute(mClient , this , mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result)  {
        if(caller instanceof GetUserAvatarRemoteOperation) {
            OnSuccessfulGetUserAvatar(caller , result);
        } else if (caller instanceof GetUserInfoRemoteOperation) {
            OnSuccessfulGetUserInfo(caller , result);
        }
    }

    private void OnSuccessfulGetUserInfo(RemoteOperation caller, RemoteOperationResult result) {
        UserInfo uinfo = (UserInfo) result.getData().get(0);

        Log.e("email" , uinfo.address);
        email.setText(uinfo.email);
        displayName.setText(uinfo.displayName);
        if (uinfo.address != null) {
            address.setText("Test");
        } else {
            address.setText("No address");
        }
    }

    private void OnSuccessfulGetUserAvatar(RemoteOperation caller , RemoteOperationResult result)  {
        GetUserAvatarRemoteOperation.ResultData r = (GetUserAvatarRemoteOperation.ResultData) result.getData().get(0);
        ArrayList<Object> data = result.getData();
        byte[] bitmapdata = r.getAvatarData();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        avatar.setImageBitmap(bitmap);
        cc.addBitmapToCache("avatar" , bitmap);
        }

    }
