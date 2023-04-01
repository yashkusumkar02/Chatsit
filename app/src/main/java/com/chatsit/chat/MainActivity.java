package com.chatsit.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.chatsit.chat.calling.RingingActivity;
import com.chatsit.chat.faceFilters.FaceFilters;
import com.chatsit.chat.fragment.CallFragment;
import com.chatsit.chat.fragment.ChatFragment;
import com.chatsit.chat.fragment.GroupFragment;
import com.chatsit.chat.fragment.StoryFragment;
import com.chatsit.chat.group.GroupProfile;
import com.chatsit.chat.groupVoiceCall.RingingGroupVoiceActivity;
import com.chatsit.chat.notification.Token;
import com.chatsit.chat.user.ProfileActivity;
import com.chatsit.chat.user.SearchActivity;
import com.chatsit.chat.user.SendToActivity;
import com.chatsit.chat.user.UserActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setNight();
            setTheme(R.style.DarkTheme);
        }else{
            setDay();
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationSelected);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new ChatFragment()).commit();

        findViewById(R.id.camera).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, FaceFilters.class)));

        AsyncTask.execute((Runnable) () -> {

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        if (ds.child("Participants").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists()){
                            for (DataSnapshot dataSnapshot1 : ds.child("Voice").getChildren()){
                                if (Objects.requireNonNull(dataSnapshot1.child("type").getValue()).toString().equals("calling")){
                                    if (!Objects.requireNonNull(dataSnapshot1.child("from").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        if (!dataSnapshot1.child("end").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            if (!dataSnapshot1.child("ans").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                Intent intent = new Intent(getApplicationContext(), RingingGroupVoiceActivity.class);
                                                intent.putExtra("room", Objects.requireNonNull(dataSnapshot1.child("room").getValue()).toString());
                                                intent.putExtra("group", Objects.requireNonNull(ds.child("groupId").getValue()).toString());
                                                startActivity(intent);
                                                finish();
                                                ref.removeEventListener(this);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("to").equalTo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (Objects.requireNonNull(ds.child("type").getValue()).toString().equals("calling")){
                                Intent intent = new Intent(getApplicationContext(), RingingActivity.class);
                                intent.putExtra("room", Objects.requireNonNull(ds.child("room").getValue()).toString());
                                intent.putExtra("from", Objects.requireNonNull(ds.child("from").getValue()).toString());
                                intent.putExtra("call", Objects.requireNonNull(ds.child("call").getValue()).toString());
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()){
                        Map<String,Object> hash = new HashMap<>();
                        hash.put("last", ""+System.currentTimeMillis());
                        hash.put("typing", "no");
                        FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(hash);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        //noinspection deprecation
        updateToken(FirebaseInstanceId.getInstance().getToken());

        findViewById(R.id.search).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SearchActivity.class)));
        findViewById(R.id.user).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type!=null){
            if ("text/plain".equals(type)){
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (intent.getStringExtra(Intent.EXTRA_SUBJECT).equals("user")){
                    Intent intent2 = new Intent(MainActivity.this, UserActivity.class);
                    intent2.putExtra("user", sharedText);
                    startActivity(intent2);
                }else if (intent.getStringExtra(Intent.EXTRA_SUBJECT).equals("group")){
                    Intent intent2 = new Intent(MainActivity.this, GroupProfile.class);
                    intent2.putExtra("group", sharedText);
                    startActivity(intent2);
                }else {
                    Intent intent3 = new Intent(MainActivity.this, SendToActivity.class);
                    intent3.putExtra("type", "text");
                    intent3.putExtra("uri", sharedText);
                    startActivity(intent3);
                }
            }else if (type.startsWith("image/")){
                Uri img = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (img != null){
                    Toast.makeText(this, "please wait...", Toast.LENGTH_SHORT).show();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_/" + ""+System.currentTimeMillis());
                    storageReference.putFile(img).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            Intent intent3 = new Intent(MainActivity.this, SendToActivity.class);
                            intent3.putExtra("type", "image");
                            intent3.putExtra("uri", downloadUri.toString());
                            startActivity(intent3);
                        }
                    });
                }else {
                    Toast.makeText(this, "null", Toast.LENGTH_SHORT).show();
                }
            }else if (type.startsWith("video/")){
                Uri img = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (img != null){
                    Toast.makeText(this, "please wait...", Toast.LENGTH_SHORT).show();
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_/" + ""+System.currentTimeMillis());
                    storageReference.putFile(img).addOnSuccessListener(taskSnapshot -> {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()){
                            Intent intent3 = new Intent(MainActivity.this, SendToActivity.class);
                            intent3.putExtra("type", "video");
                            intent3.putExtra("uri", downloadUri.toString());
                            startActivity(intent3);
                        }
                    });
                }
            }
        }

    }

    private void setDay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.gradient_bg);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    private void setNight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            Drawable background = getResources().getDrawable(R.drawable.gradient_bg_night);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }

    private void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(mToken);
    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationSelected =
            item -> {
                switch (item.getItemId()){
                    case R.id.chat:
                        selectedFragment = new ChatFragment();
                        break;
                    case R.id.group:
                        selectedFragment = new GroupFragment();
                        break;
                    case R.id.story:
                        selectedFragment = new StoryFragment();
                        break;
                    case R.id.call:
                        selectedFragment = new CallFragment();
                        break;
                }
                if (selectedFragment != null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                }
                return true;
            };

}