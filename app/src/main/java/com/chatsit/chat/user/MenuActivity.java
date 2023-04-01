package com.chatsit.chat.user;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.DeleteAccountActivity;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.TranslationActivity;
import com.chatsit.chat.auth.GenerateOTPActivity;
import com.chatsit.chat.calling.RingingActivity;
import com.chatsit.chat.groupVoiceCall.RingingGroupVoiceActivity;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MenuActivity extends AppCompatActivity {

    NightMode sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.NormalDarkTheme);
        } else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.profile).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, ProfileActivity.class)));

        TextView name = findViewById(R.id.name);
        TextView phone = findViewById(R.id.phone);
        ImageView dp = findViewById(R.id.dp);

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

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch aSwitch = findViewById(R.id.night);
        if (sharedPref.loadNightModeState()){
            aSwitch.setChecked(true);
        }
        aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.setNightModeState(isChecked);
            restartApp();
        });

        AsyncTask.execute(() -> {FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {

            assert value != null;
            name.setText(Objects.requireNonNull(value.get("name")).toString());
            phone.setText(Objects.requireNonNull(value.get("phone")).toString());
            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()) {
                Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(dp);
            }

        });

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        if (ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()){
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

        });

        findViewById(R.id.edit).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, EditProfileActivity.class)));
        findViewById(R.id.imageView).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, ProfileActivity.class)));
        findViewById(R.id.logout).setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MenuActivity.this, GenerateOTPActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.invite).setOnClickListener(view -> {
            String shareBody = getResources().getString(R.string.app_name) + " - Chatting App " + "\nDownload now on play store \nhttps://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/*");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Invite");
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intent, "Share Via"));
        });


        findViewById(R.id.blockedUser).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, BlockedUserActivity.class)));
        findViewById(R.id.translation).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, TranslationActivity.class)));
        findViewById(R.id.privacySetting).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, PrivacySettingActivity.class)));
        findViewById(R.id.notification).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, NotificationSettingActivity.class)));
        findViewById(R.id.privacy).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, PrivacyActivity.class)));
        findViewById(R.id.terms).setOnClickListener(view -> startActivity(new Intent(MenuActivity.this, TermsActivity.class)));

        findViewById(R.id.delete).setOnClickListener(view -> new AlertDialog.Builder(MenuActivity.this)
                .setTitle("Delete account")
                .setMessage("Do you really want to delete this account?")
                .setPositiveButton("Yes", (dialog, whichButton) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Please wait deleting...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MenuActivity.this, DeleteAccountActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show());

    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        Objects.requireNonNull(i).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}