package com.leydevelopment.sunibcloud.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.leydevelopment.sunibcloud.R;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider phoneAuthProvider;
    private CountryCodePicker cpp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        ImageView backBtn = (ImageView) findViewById(R.id.backBtn);
        TextView registerBtn = (TextView) findViewById(R.id.registerAuth);
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("TAG", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailText = findViewById(R.id.email);
                EditText phoneNum = findViewById(R.id.phoneNumber);
                cpp = findViewById(R.id.countryCode);
                String phoneNumbers = phoneNum.getText().toString();
                if( phoneNumbers.isEmpty()) {
                    phoneNum.setError("Please input your number!");
                } else {
                    Intent intent = new Intent(Login.this , OtpActivity.class);
                    intent.putExtra("phoneNumb" , cpp.getSelectedCountryCodeWithPlus()+phoneNumbers);
                    startActivity(intent);
                }
//                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//                    FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFir   ebaseAuthSettings();
//                    firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneNumber, smsCode);
//                    PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();
//                        phoneAuthProvider.verifyPhoneNumber(
//                            phoneNumber, 60L, TimeUnit.SECONDS, Login.this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                                @Override
//                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                                    Log.e("TAG" , "success " +phoneAuthCredential);
//                                }
//
//                                @Override
//                                public void onVerificationFailed(@NonNull FirebaseException e) {
//                                    Log.e("TAG" , "failed");
//                                }
//                            }
//                    );
            }
        });
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this , Register.class);
                startActivity(intent);
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}
