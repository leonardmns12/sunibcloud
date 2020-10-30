package com.leydevelopment.sunibcloud.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.leydevelopment.sunibcloud.R;

public class Maintenance extends AppCompatActivity {

    Button okkay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);
        okkay   = findViewById(R.id.okkay);
        getSupportActionBar().hide();
        okkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}