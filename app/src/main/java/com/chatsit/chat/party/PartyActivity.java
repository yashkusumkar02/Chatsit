package com.chatsit.chat.party;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class PartyActivity extends AppCompatActivity {

    //Permission
    private static final int VIDEO_PICK_CODE = 1002;
    private static final int PERMISSION_CODE = 1001;
    private static String videoId = "";

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party);

        findViewById(R.id.imageView).setOnClickListener(view -> onBackPressed());

        //Link
        EditText party_web_edit = findViewById(R.id.link);
        findViewById(R.id.start).setOnClickListener(v -> {

            if (party_web_edit.getText().toString().contains("youtu")){
                getVideoId(party_web_edit.getText().toString());
                webParty("upload_youtube", videoId);
            }else if (party_web_edit.getText().toString().contains("dailymotion")){
                String dmId = party_web_edit.getText().toString().replaceFirst("https://www.dailymotion.com/video/","");
                webParty("upload_dailymotion", dmId.trim());
            }else {
                Snackbar.make(v, "Paste the link from YouTube & DailyMotion", Snackbar.LENGTH_LONG).show();
            }

        });

        //Video
        findViewById(R.id.upload).setOnClickListener(v -> {
            //Check Permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){
                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else {
                    pickVideo();
                }
            }
            else {
                pickVideo();
            }
        });

    }


    private void webParty(String type, String link) {
        String room = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("from", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("room", room);
        hashMap.put("link", link);
        hashMap.put("type", type);
        FirebaseDatabase.getInstance().getReference().child("Party").child(room).setValue(hashMap).addOnCompleteListener(task -> {
            FirebaseDatabase.getInstance().getReference().child("Party").child(room).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
            String timeStamp = ""+System.currentTimeMillis();
            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("ChatId", timeStamp);
            hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap1.put("msg", "Started the watch party");
            FirebaseDatabase.getInstance().getReference().child("Party").child(room).child("Chats").child(timeStamp).setValue(hashMap1);
        });
        Intent intent = new Intent(getApplicationContext(), InviteActivity.class);
        intent.putExtra("room", room);
        startActivity(intent);
        finish();
    }

    public static String getVideoId(@NonNull String videoUrl) {
        String regex = "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|be\\.com\\/(?:watch\\?(?:feature=youtu.be\\&)?v=|v\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);
        if(matcher.find()){
            videoId = matcher.group(1);
        }
        return videoId;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(findViewById(R.id.main), "Storage permission allowed", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(findViewById(R.id.main), "Storage permission is required", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, VIDEO_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK && requestCode == VIDEO_PICK_CODE && data != null){
           Uri video_uri = Objects.requireNonNull(data).getData();
           sendVideo(video_uri);
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            Snackbar.make(findViewById(R.id.main), "Please wait, uploading...", Snackbar.LENGTH_LONG).show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendVideo(Uri videoUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("party_video/" + ""+System.currentTimeMillis());
        storageReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                String room = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("from", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("room", room);
                hashMap.put("privacy", "");
                hashMap.put("link", downloadUri.toString());
                hashMap.put("type", "upload_video");
                FirebaseDatabase.getInstance().getReference().child("Party").child(room).setValue(hashMap).addOnCompleteListener(task -> {
                    FirebaseDatabase.getInstance().getReference().child("Party").child(room).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                    String timeStamp = ""+System.currentTimeMillis();
                    HashMap<String, Object> hashMap1 = new HashMap<>();
                    hashMap1.put("ChatId", timeStamp);
                    hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    hashMap1.put("msg", "Started the watch party");
                    FirebaseDatabase.getInstance().getReference().child("Party").child(room).child("Chats").child(timeStamp).setValue(hashMap1);
                });
                findViewById(R.id.progress).setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), InviteActivity.class);
                intent.putExtra("room", room);
                startActivity(intent);
                finish();
            }
        });
    }

}