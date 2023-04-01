package com.chatsit.chat.party;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dailymotion.android.player.sdk.PlayerWebView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.MainActivity;
import com.chatsit.chat.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class WatchPartyActivity extends AppCompatActivity {

    private static String id;
    VideoView videoView;


    public static String getId() {
        return id;
    }
    public WatchPartyActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_watch_party);

        id = getIntent().getStringExtra("room");
        videoView = findViewById(R.id.videoView);

        PlayerWebView dailyMotionPlayer= findViewById(R.id.dailymotionPlayer);

        FirebaseDatabase.getInstance().getReference().child("Party").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //UserInfo
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                    TextView username = findViewById(R.id.username);
                    username.setText(value.get("name").toString());
                    CircleImageView photo = findViewById(R.id.mDp);
                    if (!value.get("photo").toString().isEmpty()){
                        Picasso.get().load(value.get("photo").toString()).into(photo);
                    }
                });

                TextView live = findViewById(R.id.number);
                live.setText(String.valueOf( snapshot.child("users").getChildrenCount()));

                //Video
                if (snapshot.child("type").getValue().toString().equals("upload_video") || snapshot.child("type").getValue().toString().equals("video")){

                    //Hide
                    videoView.setVisibility(View.VISIBLE);
                    dailyMotionPlayer.setVisibility(View.GONE);

                    videoView.setVideoURI(Uri.parse(snapshot.child("link").getValue().toString()));
                    videoView.start();
                    videoView.setOnPreparedListener(mp -> mp.setLooping(true));
                    MediaController mediaController = new MediaController(WatchPartyActivity.this);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);

                    findViewById(R.id.chat).setOnClickListener(v -> {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WatchPartyActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("time", videoView.getCurrentPosition());
                        editor.apply();
                        videoView.pause();
                        PartyChatActivity dialogFragment = PartyChatActivity.newInstance();
                        dialogFragment.setCallBack((type, value) -> {
                            if (value.equals("sent")){
                                int time = preferences.getInt("time", 0);
                                videoView.seekTo(time);
                                videoView.start();
                            }else {
                                videoView.resume();
                            }
                        });
                        dialogFragment.show(getSupportFragmentManager(), "tag");

                    });

                }
                else if (snapshot.child("type").getValue().toString().equals("upload_dailymotion")){
                    //Hide
                    videoView.setVisibility(View.GONE);
                    dailyMotionPlayer.setVisibility(View.VISIBLE);
                    dailyMotionPlayer.load(snapshot.child("link").getValue().toString());

                    findViewById(R.id.chat).setOnClickListener(v -> {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WatchPartyActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putLong("time", dailyMotionPlayer.getPosition());
                        editor.apply();
                        dailyMotionPlayer.pause();
                        PartyChatActivity dialogFragment = PartyChatActivity.newInstance();
                        dialogFragment.setCallBack((type, value) -> {
                            if (value.equals("sent")){
                                int time = preferences.getInt("time", 0);
                                dailyMotionPlayer.seek(time);
                                dailyMotionPlayer.play();
                            }else {
                              dailyMotionPlayer.onResume();
                            }
                        });
                        dialogFragment.show(getSupportFragmentManager(), "tag");

                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        hideTimer();
        findViewById(R.id.main).setOnClickListener(v -> {
            findViewById(R.id.live_room_top_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.number).setVisibility(View.VISIBLE);
            findViewById(R.id.menu).setVisibility(View.VISIBLE);
            hideTimer();
        });

        findViewById(R.id.invite).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), InviteMoreActivity.class);
            intent.putExtra("room", id);
            startActivity(intent);
        });

        findViewById(R.id.change).setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ChangeWatchPartyActivity.class);
            intent.putExtra("room", id);
            startActivity(intent);
        });


        findViewById(R.id.close).setOnClickListener(v -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WatchPartyActivity.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("time");
            editor.apply();
            FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
            String timeStamp = ""+ System.currentTimeMillis();
            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("ChatId", timeStamp);
            hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap1.put("msg", "has left");
            FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats").child(timeStamp).setValue(hashMap1);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });


    }


    private void hideTimer() {
        new Handler().postDelayed(() -> {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            findViewById(R.id.live_room_top_layout).setVisibility(View.GONE);
            findViewById(R.id.number).setVisibility(View.GONE);
            findViewById(R.id.menu).setVisibility(View.GONE);
        },2000);
    }

}