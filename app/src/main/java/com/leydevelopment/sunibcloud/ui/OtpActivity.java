package com.leydevelopment.sunibcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.leydevelopment.sunibcloud.MainActivity;
import com.leydevelopment.sunibcloud.R;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    PinView pinView;
    String otpCode;
    String phoneNumb;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        getSupportActionBar().hide();
        phoneNumb = getIntent().getExtras().getString("phoneNumb");
        TextView otpText = findViewById(R.id.otpText);
        otpText.setText("Please type verification code sent to " + phoneNumb);
        Button button = findViewById(R.id.verify);
        db = FirebaseFirestore.getInstance();
        pinView = findViewById(R.id.firstPinView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode(pinView.getText().toString());
            }
        });
        if(pinView.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        
        sendVerificationToUser(phoneNumb);

    }

    private void sendVerificationToUser(String phoneNumb) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumb,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                TaskExecutors.MAIN_THREAD,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    otpCode = s;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        pinView.setText(code);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.e("ERR" , e.getMessage());
                }
            };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpCode , code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(OtpActivity.this , "Verification Success!", Toast.LENGTH_SHORT).show();
                            checkCurrentUsers();

                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(OtpActivity.this , "Verification failed, Try again...!", Toast.LENGTH_SHORT).show();
                                pinView.setText("");
                            }
                        }
                    }
                });
    }

    private void addNewUsers() {

        Map<String, Object> user = new HashMap<>();
        user.put("Completed Registration", false);
        db.document("users/"+phoneNumb).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(OtpActivity.this , TaskFragment.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OtpActivity.this , "Failed to connect to server!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OtpActivity.this , Login.class);
                startActivity(intent);
            }
        });
    }

    private void checkCurrentUsers() {
        DocumentReference docRef = db.collection("users").document(phoneNumb);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        Intent intent = new Intent(OtpActivity.this , MainActivity.class);
                        startActivity(intent);
                    } else {
                       addNewUsers();
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }
}