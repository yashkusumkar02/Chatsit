package com.chatsit.chat.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.MainActivity;
import com.chatsit.chat.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText name = findViewById(R.id.name);
        EditText email = findViewById(R.id.email);
        EditText bio = findViewById(R.id.bio);

        findViewById(R.id.signUp).setOnClickListener(view -> {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            if (Objects.requireNonNull(name.getText()).toString().trim().isEmpty()){
                Snackbar.make(view, "Enter name", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progress).setVisibility(View.GONE);
            }else  if (Objects.requireNonNull(email.getText()).toString().trim().isEmpty()){
                Snackbar.make(view, "Enter email", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progress).setVisibility(View.GONE);
            }else  if (Objects.requireNonNull(bio.getText()).toString().trim().isEmpty()){
                Snackbar.make(view, "Enter bio", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progress).setVisibility(View.GONE);
            }else {

                Map<String,Object> hashMap = new HashMap<>();
                hashMap.put("name", name.getText().toString().trim());
                hashMap.put("email", email.getText().toString().trim());
                hashMap.put("bio", bio.getText().toString().trim());
                hashMap.put("id", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("phone", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                hashMap.put("photo", "");
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(hashMap);

                Map<String,Object> hash = new HashMap<>();
                hash.put("last", ""+System.currentTimeMillis());
                hash.put("typing", "no");
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(hash);

                findViewById(R.id.progress).setVisibility(View.GONE);
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }

        });

    }
}