package com.chatsit.chat.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class EditProfileActivity extends AppCompatActivity {

    CircleImageView dp;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Back
        findViewById(R.id.imageView).setOnClickListener(view -> onBackPressed());

        MobileAds.initialize(getApplicationContext(), initializationStatus -> {
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        FirebaseDatabase.getInstance().getReference().child("Ads").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("on")){
                    mAdView.setVisibility(View.VISIBLE);
                }else {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Image
        dp = findViewById(R.id.dp);
       dp.setOnClickListener(view -> {
           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
               if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                       == PackageManager.PERMISSION_DENIED){
                   String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                   requestPermissions(permissions, 100);
               }
               else {
                   pickImage();
               }
           }
           else {
               pickImage();
           }
       });

        //EditText
        EditText name = findViewById(R.id.name);
        EditText bio = findViewById(R.id.bio);
        EditText email = findViewById(R.id.email);
        EditText phone = findViewById(R.id.phone);

        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {

            name.setText(Objects.requireNonNull(value.get("name")).toString());
            email.setText(Objects.requireNonNull(value.get("email")).toString());
            bio.setText(Objects.requireNonNull(value.get("bio")).toString());
            phone.setText(Objects.requireNonNull(value.get("phone")).toString());
            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(dp);
            }

        });

        findViewById(R.id.save).setOnClickListener(view -> {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            if (name.getText().toString().isEmpty()){
                findViewById(R.id.progress).setVisibility(View.GONE);
                Snackbar.make(view, "Please enter your name", Snackbar.LENGTH_LONG).show();
            }else if (bio.getText().toString().isEmpty()){
                findViewById(R.id.progress).setVisibility(View.GONE);
                Snackbar.make(view, "Please enter your bio", Snackbar.LENGTH_LONG).show();
            }else if (email.getText().toString().isEmpty()){
                findViewById(R.id.progress).setVisibility(View.GONE);
                Snackbar.make(view, "Please enter your email", Snackbar.LENGTH_LONG).show();
            }else if (phone.getText().toString().isEmpty()){
                findViewById(R.id.progress).setVisibility(View.GONE);
                Snackbar.make(view, "Please enter your phone no.", Snackbar.LENGTH_LONG).show();
            }else {

                Map<String,Object> hashMap = new HashMap<>();
                hashMap.put("name", name.getText().toString().trim());
                hashMap.put("email", email.getText().toString().trim());
                hashMap.put("bio", bio.getText().toString().trim());
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(hashMap);
                findViewById(R.id.progress).setVisibility(View.GONE);

                if (!Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()).equals(phone.getText().toString())){

                   Intent intent = new Intent(EditProfileActivity.this, OTPEditActivity.class);
                   intent.putExtra("phone", phone.getText().toString().trim());
                   startActivity(intent);
                   findViewById(R.id.progress).setVisibility(View.GONE);

                }else {
                    findViewById(R.id.progress).setVisibility(View.GONE);
                    Snackbar.make(view, "Saved", Snackbar.LENGTH_LONG).show();
                }

            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200 && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            Picasso.get().load(dp_uri).into(dp);
            uploadDp(dp_uri);
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            Snackbar.make(dp, "Please wait, uploading...", Snackbar.LENGTH_LONG).show();
        }
    }

    private void uploadDp(Uri dp_uri) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(dp_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                Map<String,Object> hashMap = new HashMap<>();
                hashMap.put("photo", downloadUri.toString());
                FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).update(hashMap);
                Snackbar.make(dp, "Profile photo updated", Snackbar.LENGTH_LONG).show();
                findViewById(R.id.progress).setVisibility(View.GONE);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Snackbar.make(dp, "Permission is required", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 200);
    }
}