package com.leydevelopment.sunibcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.leydevelopment.sunibcloud.R;

public class Authentication extends AppCompatActivity {
    Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        getSupportActionBar().hide();
        TextView registerBtn = (TextView)findViewById(R.id.registerAuth);
        loginBtn = findViewById(R.id.loginAuth);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Authentication.this , Login.class);
                startActivity(intent);
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Authentication.this , Register.class);
                startActivity(intent);
            }
        });
    }
}
