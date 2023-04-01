package com.chatsit.chat.party;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.MainActivity;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterPartyChat;
import com.chatsit.chat.model.ModelPartyChat;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


import de.hdodenhof.circleimageview.CircleImageView;

public class YouTubePartyActivity extends YouTubeBaseActivity {

    public static final String YOUR_API_KEY = "AIzaSyB7j0nWf49cPUwwwolCWs7saxLVjcRbjjs";
    String id;

    EditText sendMessage;
    ImageView send;
    RecyclerView chat_rv;

    //Post
    AdapterPartyChat partyChat;
    List<ModelPartyChat> modelPartyChats;

    @Override
    public void onBackPressed() {
        FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).removeValue();
        String timeStamp = ""+ System.currentTimeMillis();
        HashMap<String, Object> hashMap1 = new HashMap<>();
        hashMap1.put("ChatId", timeStamp);
        hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap1.put("msg", "has left");
        FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats").child(timeStamp).setValue(hashMap1);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_you_tube_party);

        id  = getIntent().getStringExtra("room");

        YouTubePlayerView youTubePlayerView = findViewById(R.id.YouTubePlayer);

        FirebaseDatabase.getInstance().getReference().child("Party").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(snapshot.child("from").getValue()).toString()).addSnapshotListener((value, error) -> {

                    TextView username = findViewById(R.id.username);
                    assert value != null;
                    username.setText(Objects.requireNonNull(value.get("name")).toString());
                    String mDp = Objects.requireNonNull(value.get("photo")).toString();
                    CircleImageView photo = findViewById(R.id.mDp);
                    if (!mDp.isEmpty()){
                        Picasso.get().load(mDp).placeholder(R.drawable.avatar).into(photo);
                    }

                });

                TextView live = findViewById(R.id.number);
                live.setText(String.valueOf( snapshot.child("users").getChildrenCount()));

                youTubePlayerView.initialize(YOUR_API_KEY, new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                        //Video
                        youTubePlayer.loadVideo(Objects.requireNonNull(snapshot.child("link").getValue()).toString());
                        youTubePlayer.play();
                    }
                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.chat).setOnClickListener(v -> {
            findViewById(R.id.chatView).setVisibility(View.VISIBLE);
            youTubePlayerView.setVisibility(View.GONE);
        });

        findViewById(R.id.imageView).setOnClickListener(v -> {
            findViewById(R.id.chatView).setVisibility(View.GONE);
            youTubePlayerView.setVisibility(View.VISIBLE);
        });

        //Id
        sendMessage = findViewById(R.id.editText);
        send = findViewById(R.id.message_send);
        chat_rv = findViewById(R.id.chat_rv);

        modelPartyChats = new ArrayList<>();

        chat_rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        chat_rv.setLayoutManager(linearLayoutManager);

        send.setOnClickListener(v1 -> {
            String msg = sendMessage.getText().toString();
            if (msg.isEmpty()){
                Snackbar.make(v1, "Type a message to send", Snackbar.LENGTH_LONG).show();
            }else {

                String timeStamp = ""+ System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("ChatId", timeStamp);
                hashMap.put("userId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("msg", msg);
                FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats").child(timeStamp).setValue(hashMap);

                sendMessage.setText("");

            }
        });

        readMessage();

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
            FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).removeValue();
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

    private void readMessage() {
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child("Party").child(id).child("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPartyChats.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPartyChat modelLiveChat = ds.getValue(ModelPartyChat.class);
                    modelPartyChats.add(modelLiveChat);
                }
                partyChat = new AdapterPartyChat(getApplicationContext(), modelPartyChats);
                chat_rv.setAdapter(partyChat);
                partyChat.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}