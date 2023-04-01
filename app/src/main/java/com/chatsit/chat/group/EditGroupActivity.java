package com.chatsit.chat.group;

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

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
public class EditGroupActivity extends AppCompatActivity {

    CircleImageView dp;
    String groupId;
    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        groupId = getIntent().getStringExtra("group");

        //Back
        findViewById(R.id.imageView).setOnClickListener(view -> onBackPressed());

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

        FirebaseFirestore.getInstance().collection("groups").document(groupId).addSnapshotListener((value, error) -> {

            name.setText(Objects.requireNonNull(value.get("name")).toString());
            bio.setText(Objects.requireNonNull(value.get("bio")).toString());
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
            }else {

                Map<String,Object> hashMap = new HashMap<>();
                hashMap.put("name", name.getText().toString().trim());
                hashMap.put("bio", bio.getText().toString().trim());
                FirebaseFirestore.getInstance().collection("groups").document(groupId).update(hashMap);
                findViewById(R.id.progress).setVisibility(View.GONE);

                findViewById(R.id.progress).setVisibility(View.GONE);
                Snackbar.make(view, "Saved", Snackbar.LENGTH_LONG).show();

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
                FirebaseFirestore.getInstance().collection("groups").document(groupId).update(hashMap);
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