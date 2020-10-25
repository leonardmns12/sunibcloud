package com.leydevelopment.sunibcloud;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.leydevelopment.sunibcloud.ui.Authentication;
import com.leydevelopment.sunibcloud.ui.HistoryFragment;
import com.leydevelopment.sunibcloud.ui.HomeFragment;
import com.leydevelopment.sunibcloud.ui.SettingFragment;
import com.leydevelopment.sunibcloud.ui.TaskFragment;
import com.leydevelopment.sunibcloud.utils.BottomSheet;
import com.leydevelopment.sunibcloud.utils.FileBottomDialog;

import java.io.File;

public class MainActivity extends AppCompatActivity implements  BottomSheet.BottomSheetListener , FileBottomDialog.FileDialogListner {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null) {
            Intent intent = new Intent(this , Authentication.class);
            startActivity(intent);
        } else {
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnNavigationItemSelectedListener(navListener);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
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
}
