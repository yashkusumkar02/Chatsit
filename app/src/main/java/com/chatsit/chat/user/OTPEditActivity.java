package com.chatsit.chat.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTPEditActivity extends AppCompatActivity {

    private String verificationId;
    EditText otp;
    private FirebaseAuth mAuth;
    String phonenumber;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.NormalDarkTheme);
        } else setTheme(R.style.NormalDayTheme);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        mAuth = FirebaseAuth.getInstance();

        phonenumber = getIntent().getStringExtra("phone");
        sendVerificationCode(phonenumber);

        //Back
        findViewById(R.id.imageView).setVisibility(View.GONE);

        //EditText
        otp = findViewById(R.id.otp);

        //Button
        findViewById(R.id.signIn).setOnClickListener(v -> {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            String code = otp.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6){
                Snackbar.make(v,"Enter OTP", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progress).setVisibility(View.INVISIBLE);
            }else {
                verifyCode(code);
            }

        });

    }

    private  void verifyCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Objects.requireNonNull(mAuth.getCurrentUser()).updatePhoneNumber(credential)
                .addOnCompleteListener(OTPEditActivity.this, task -> {
                    if (task.isSuccessful()) {
                        Map<String,Object> hashMap = new HashMap<>();
                        hashMap.put("phone", phonenumber);
                        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).update(hashMap);
                        findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                        Toast.makeText(this, "Phone no. changed", Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(OTPEditActivity.this, msg, Toast.LENGTH_SHORT).show();
                        findViewById(R.id.progress).setVisibility(View.INVISIBLE);
                    }
                    startActivity(new Intent(OTPEditActivity.this, EditProfileActivity.class));
                    finish();
                });
    }

    private void sendVerificationCode(String phonenumber) {
        //noinspection deprecation
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenumber,
                60,
                TimeUnit.SECONDS,
                OTPEditActivity.this,
                mCallbacks);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null){
                otp.setText(code);
                verifyCode(code);
                findViewById(R.id.progress).setVisibility(View.VISIBLE);
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OTPEditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        }
    };

}