package com.leydevelopment.sunibcloud;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.leydevelopment.sunibcloud.ui.Authentication;
import com.leydevelopment.sunibcloud.ui.ConnectAccount;
import com.leydevelopment.sunibcloud.ui.HistoryFragment;
import com.leydevelopment.sunibcloud.ui.HomeFragment;
import com.leydevelopment.sunibcloud.ui.NoNetwork;
import com.leydevelopment.sunibcloud.ui.SettingFragment;
import com.leydevelopment.sunibcloud.ui.TaskFragment;
import com.leydevelopment.sunibcloud.utils.BottomSheet;
import com.leydevelopment.sunibcloud.utils.FileBottomDialog;
import com.leydevelopment.sunibcloud.utils.MyApp;
import com.leydevelopment.sunibcloud.utils.NetworkConnectivity;

import java.io.File;

public class MainActivity extends AppCompatActivity implements  BottomSheet.BottomSheetListener , FileBottomDialog.FileDialogListner , NetworkConnectivity.NetworkConnectivityListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkInternetConnection();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        checkCurrentUsers();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            Intent intent = new Intent(this , Authentication.class);
            startActivity(intent);
        } else {
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
                bottomNav.setOnNavigationItemSelectedListener(navListener);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            }
            else {
            }
        }
    }

    private void checkInternetConnection() {
        NetworkConnectivity nc = new NetworkConnectivity();
        boolean isConnected = nc.isConnected(this);
        showSnackbar(isConnected);

        if(!isConnected) {
            changeActivity();
        }
    }

    private void changeActivity() {
            Intent intent = new Intent(this , NoNetwork.class);
            startActivity(intent);
    }

    private void showSnackbar(boolean isConnected) {
        String message;
        int color;

        if(isConnected) {
            message = "You are online...";
            color = Color.WHITE;
        } else {
            message = "You are offline...";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar.make(findViewById(R.id.RL) , message , Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        NetworkConnectivity networkConnectivity = new NetworkConnectivity();
        registerReceiver(networkConnectivity,intentFilter);

        MyApp.getInstance().setNetworkConnectionListener(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_task:
                            selectedFragment = new TaskFragment();
                            break;
                        case R.id.nav_history:
                            selectedFragment = new HistoryFragment();
                            break;
                        case R.id.nav_setting:
                            selectedFragment = new SettingFragment();
                        }
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
            };

    @Override
    public void onFilesTaken(String name , int size , File fileUri) {
        TaskFragment.receiverMethod(name,size,fileUri);
    }

    @Override
    public void onActionTaken(String name, String rPath) {
        HistoryFragment.receiverMethod(name , rPath);
        Intent intent = new Intent(MainActivity.this , HistoryFragment.class);
        if(name.equals("Remove") || name.equals("Rename")){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).commit();
        }
    }

    private void checkCurrentUsers() {
        DocumentReference docRef = db.collection("users").document("+6281290404447");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                    } else {
                        Intent intent = new Intent(MainActivity.this, ConnectAccount.class);
                        startActivity(intent);
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public void onNetworkConnectivityChanged(boolean isConnected) {
        if(!isConnected){
            changeActivity();
        }
        showSnackbar(isConnected);
    }
}
