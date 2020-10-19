package com.leydevelopment.sunibcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.leydevelopment.sunibcloud.R;


public class Register extends AppCompatActivity {
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();
        ImageView backBtn = (ImageView) findViewById(R.id.backBtn);
        Button button = (Button) findViewById(R.id.registerBtn);
        TextView loginBtn = (TextView) findViewById(R.id.loginAuth);
        final EditText emailView = (EditText) findViewById(R.id.email);
        final EditText pwdView = (EditText) findViewById(R.id.password);
        final EditText pwdConfView = (EditText) findViewById(R.id.password_confirm);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                String email = emailView.getText().toString();
                String password = pwdView.getText().toString();
                String passwordConf = pwdConfView.getText().toString();
                if ( email.length() < 1) {
                    emailView.setError("Email cannot empty!");
                }
                else if (password.length() < 6) {
                    pwdView.setError("Password must be at least 6 character");
                    return;
                }
                else if (!password.equals(passwordConf)) {
                    pwdConfView.setError("Password didn't match!");
                    return;
                } else {
                    mAuth.createUserWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Register.this, "Authentication Success.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, task.getException().toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Register.this , Login.class);
                startActivity(intent);
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
