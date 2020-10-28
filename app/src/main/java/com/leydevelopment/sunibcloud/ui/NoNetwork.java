package com.leydevelopment.sunibcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.leydevelopment.sunibcloud.MainActivity;
import com.leydevelopment.sunibcloud.R;

public class NoNetwork extends AppCompatActivity {

    private Button retry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_network);
        getSupportActionBar().hide();
        retry   = findViewById(R.id.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoNetwork.this , MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}