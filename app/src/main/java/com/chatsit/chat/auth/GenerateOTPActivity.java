package com.chatsit.chat.auth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.hbb20.CountryCodePicker;
import com.chatsit.chat.R;

import java.util.Objects;

public class GenerateOTPActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_o_t_p);

        if (ContextCompat.checkSelfPermission(GenerateOTPActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(GenerateOTPActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }

        EditText phone = findViewById(R.id.phone);

        CountryCodePicker ccp = findViewById(R.id.code);
        String code = ccp.getSelectedCountryCode();
        phone.setText("+"+code);

        findViewById(R.id.signIn).setOnClickListener(view -> {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            if (Objects.requireNonNull(phone.getText()).toString().trim().isEmpty()){
                Snackbar.make(view, "Please enter your phone no.", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progress).setVisibility(View.GONE);
            }else {

                Intent intent = new Intent(GenerateOTPActivity.this, VerifyActivity.class);
                intent.putExtra("phone", phone.getText().toString().trim());
                startActivity(intent);
                findViewById(R.id.progress).setVisibility(View.GONE);
            }


        });

    }
}