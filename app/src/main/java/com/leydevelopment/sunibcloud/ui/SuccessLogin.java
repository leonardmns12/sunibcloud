package com.leydevelopment.sunibcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.leydevelopment.sunibcloud.MainActivity;
import com.leydevelopment.sunibcloud.R;

public class SuccessLogin extends AppCompatActivity {
    private Button dashboardBtn;
    private ImageView imageIcon;
    private String SUCCESS_ID;
    private TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_login);
        getSupportActionBar().hide();
        dashboardBtn    = (Button) findViewById(R.id.dashboardBtn);
        imageIcon       = (ImageView) findViewById(R.id.imageIcon);
        title           = (TextView) findViewById(R.id.title);
        SUCCESS_ID      = getIntent().getStringExtra("SUCCESS_ID");

        if(SUCCESS_ID.equals("otp")) {
            imageIcon.setImageResource(R.drawable.confirmed);
            title.setText("Authentication code success");
        }
        dashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuccessLogin.this , MainActivity.class);
                startActivity(intent);
            }
        });
    }
}