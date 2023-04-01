package com.chatsit.chat.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.chatsit.chat.MainActivity;
import com.chatsit.chat.R;
import com.chatsit.chat.faceFilters.FaceFilters;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;


@SuppressWarnings("ALL")
public class AddStoryActivity extends AppCompatActivity {

    //Uri
    Uri imgUri,videoUri;

    //Id
    ImageView image;
    VideoView video;

    //Strings
    String type;

    @SuppressLint("IntentReset")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_story);

        //back
        findViewById(R.id.back).setOnClickListener(v -> {
            Intent i = new Intent(AddStoryActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

        //Camera
        findViewById(R.id.camera).setOnClickListener(v -> startActivity(new Intent(AddStoryActivity.this, FaceFilters.class)));

        //Gallery
        findViewById(R.id.gallery).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 12);
        });

        findViewById(R.id.video).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(intent, 22);
        });

        //Id
        image = findViewById(R.id.image);
        video = findViewById(R.id.videoView);


        if (getIntent().hasExtra("type")){
            String uri = getIntent().getStringExtra("uri");
            if (getIntent().getStringExtra("type").equals("image")){
                image.setVisibility(View.VISIBLE);
                video.setVisibility(View.GONE);
                Picasso.get().load(uri).into(image);
                type = "image";
            }else {
                video.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                type = "video";
                video.setVideoURI(Uri.parse(uri));
                video.start();
                video.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                });
            }
        }

        //post
        findViewById(R.id.post).setOnClickListener(v -> {
            if (imgUri == null && videoUri == null) {
                Snackbar.make(v, "Add a image or video", Snackbar.LENGTH_SHORT).show();
            }else {

                if (type.equals("image")) {
                    uploadImage();
                    Snackbar.make(v, "Please wait...", Snackbar.LENGTH_SHORT).show();
                } else if (type.equals("video")) {
                    uploadVideo(videoUri);
                    Snackbar.make(v, "Please wait...", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(v, "Add a image or video", Snackbar.LENGTH_SHORT).show();
                }


            }
        });

    }

    private void uploadVideo(Uri videoUri) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        String storyId = reference.push().getKey();
        long timeend = System.currentTimeMillis()+86400000;

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Story/" + "Story_" + timeStamp;
        FirebaseStorage.getInstance().getReference().child(filePathAndName).putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();
            if (uriTask.isSuccessful()) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageUri", downloadUri);
                hashMap.put("timestart", ServerValue.TIMESTAMP);
                hashMap.put("timeend", timeend);
                hashMap.put("storyid", storyId);
                hashMap.put("type", "video");
                hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                assert storyId != null;
                reference.child(storyId).setValue(hashMap);

                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                type = "";

                Snackbar.make(findViewById(R.id.main), "Story uploaded", Snackbar.LENGTH_LONG).show();

            }
        });

    }

    private void uploadImage() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        String storyId = reference.push().getKey();
        long timeend = System.currentTimeMillis()+86400000;

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Story/" + "Story_" + timeStamp;
        FirebaseStorage.getInstance().getReference().child(filePathAndName).putFile(imgUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            String downloadUri = Objects.requireNonNull(uriTask.getResult()).toString();
            if (uriTask.isSuccessful()) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageUri", downloadUri);
                hashMap.put("timestart", ServerValue.TIMESTAMP);
                hashMap.put("timeend", timeend);
                hashMap.put("storyid", storyId);
                hashMap.put("type", "image");
                hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                assert storyId != null;
                reference.child(storyId).setValue(hashMap);

                video.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                type = "";

                Snackbar.make(findViewById(R.id.main), "Story uploaded", Snackbar.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 12) {
            imgUri = Objects.requireNonNull(data).getData();
            image.setVisibility(View.VISIBLE);
            video.setVisibility(View.GONE);
            Picasso.get().load(imgUri).into(image);
            type = "image";

        }else if (resultCode == RESULT_OK && requestCode == 22) {
            videoUri = Objects.requireNonNull(data).getData();
            video.setVisibility(View.VISIBLE);
            image.setVisibility(View.GONE);
            type = "video";
            video.setVideoURI(videoUri);
            video.start();
            video.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                mp.setVolume(0, 0);
            });
        }else {
            type = "";
            video.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
        }
    }

}