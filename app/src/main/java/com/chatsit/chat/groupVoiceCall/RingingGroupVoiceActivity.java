package com.chatsit.chat.groupVoiceCall;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.R;
import com.chatsit.chat.group.GroupChatActivity;
import com.chatsit.chat.groupVoiceCall.openacall.model.ConstantApp;
import com.chatsit.chat.groupVoiceCall.openacall.ui.BaseActivity;
import com.chatsit.chat.groupVoiceCall.openacall.ui.VoiceCallGroupActivity;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class RingingGroupVoiceActivity extends BaseActivity {
    String groupId,room;
    MediaPlayer mp;
    private void setDay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.gradient_bg);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
setDay();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_group_video);

        mp = MediaPlayer.create(this, R.raw.ringtone);
        mp.start();

        groupId = getIntent().getStringExtra("group");
        room = getIntent().getStringExtra("room");

        //Query
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("cancel") && !snapshot.child("end").hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    mp.stop();
                    Toast.makeText(RingingGroupVoiceActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RingingGroupVoiceActivity.this, GroupChatActivity.class);
                    intent.putExtra("group", groupId);
                    intent.putExtra("type", "create");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseFirestore.getInstance().collection("groups").document(groupId).addSnapshotListener((value, error) -> {
            TextView name = findViewById(R.id.name);
            name.setText(Objects.requireNonNull(value.get("name")).toString());
            CircleImageView dp = findViewById(R.id.dp);
            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty())  Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(dp);
        });


        findViewById(R.id.end).setOnClickListener(v -> {
            mp.stop();
            Toast.makeText(RingingGroupVoiceActivity.this, "Declined", Toast.LENGTH_SHORT).show();
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).child("end").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
            Intent intent = new Intent(RingingGroupVoiceActivity.this, GroupChatActivity.class);
            intent.putExtra("group", groupId);
            intent.putExtra("type", "create");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        });

        findViewById(R.id.pick).setOnClickListener(v -> {
            mp.stop();
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).child("ans").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
            vSettings().mChannelName = room;
            Intent i = new Intent(RingingGroupVoiceActivity.this, VoiceCallGroupActivity.class);
            i.putExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME, room);
            i.putExtra("group", groupId);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();

        });


    }

    @Override
    protected void initUIandEvent() {

    }

    @Override
    protected void deInitUIandEvent() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mp.stop();
        Toast.makeText(RingingGroupVoiceActivity.this, "Declined", Toast.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room).child("end").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);
        Intent intent = new Intent(RingingGroupVoiceActivity.this, GroupChatActivity.class);
        intent.putExtra("group", groupId);
        intent.putExtra("type", "create");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}