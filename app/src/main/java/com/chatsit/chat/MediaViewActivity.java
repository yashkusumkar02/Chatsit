package com.chatsit.chat;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;


public class MediaViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_media_view);

        //GetUri
        String uri = getIntent().getStringExtra("uri");
        String type = getIntent().getStringExtra("type");

        //Video
        if (type.equals("video")){
            VideoView videoView = findViewById(R.id.videoView);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(uri));
            videoView.start();
            videoView.setOnPreparedListener(mp -> mp.setLooping(true));
            MediaController mediaController = new MediaController(MediaViewActivity.this);
            mediaController.setAnchorView(videoView);
            findViewById(R.id.pb).setVisibility(View.VISIBLE);
            videoView.setMediaController(mediaController);
            videoView.setOnPreparedListener(mp -> {
                mp.start();
                mp.setOnVideoSizeChangedListener((mp1, arg1, arg2) -> {
                    findViewById(R.id.pb).setVisibility(View.GONE);
                    mp1.start();
                });
            });
        }else if (type.equals("image")){
            ImageView photoView = findViewById(R.id.photo_view);
            photoView.setVisibility(View.VISIBLE);
            Picasso.get().load(uri).into(photoView);
        }


    }
}