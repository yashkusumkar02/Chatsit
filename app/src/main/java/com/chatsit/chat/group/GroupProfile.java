package com.chatsit.chat.group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.MainActivity;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterParticipants;
import com.chatsit.chat.calling.RingingActivity;
import com.chatsit.chat.groupVoiceCall.RingingGroupVoiceActivity;
import com.chatsit.chat.model.UserModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GroupProfile extends AppCompatActivity {

    String groupId;
    List<String> list;
    private RecyclerView users_rv;
    private List<UserModel> userList;
    private AdapterParticipants adapterUsers;
    String myRole;
    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        groupId = getIntent().getStringExtra("group");

        findViewById(R.id.imageView).setOnClickListener(view -> onBackPressed());

        TextView name = findViewById(R.id.name);
        TextView bio = findViewById(R.id.bio);
        ImageView dp= findViewById(R.id.dp);
        TextView count = findViewById(R.id.count);

        users_rv = findViewById(R.id.members);
        users_rv.setLayoutManager(new LinearLayoutManager(GroupProfile.this));
        userList = new ArrayList<>();

        AsyncTask.execute(() -> {
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
            FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Participants").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                myRole = ""+snapshot.child("role").getValue();
                                list = new ArrayList<>();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                                ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        list.clear();
                                        for (DataSnapshot ds: snapshot.getChildren()){
                                            list.add(ds.getKey());
                                        }
                                        getUser();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                if (myRole.equals("creator")){
                                    findViewById(R.id.delete).setVisibility(View.VISIBLE);
                                    findViewById(R.id.leave).setVisibility(View.GONE);
                                }else {
                                    findViewById(R.id.delete).setVisibility(View.GONE);
                                    findViewById(R.id.leave).setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            FirebaseFirestore.getInstance().collection("groups").document(groupId).addSnapshotListener((value, error) -> {

                assert value != null;
                name.setText(Objects.requireNonNull(value.get("name")).toString());
                bio.setText(Objects.requireNonNull(value.get("bio")).toString());
                if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                    Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(dp);
                }

            });

            FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    count.setText(snapshot.getChildrenCount()+" Members");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });

        findViewById(R.id.createPost).setOnClickListener(view -> {
            Intent i = new Intent(this, EditGroupActivity.class);
            i.putExtra("group", groupId);
            startActivity(i);
        });

        findViewById(R.id.delete).setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfile.this);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure to delete this group ?");
            builder.setPositiveButton("Delete", (dialog, which) -> {

                FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).getRef().removeValue();
                FirebaseFirestore.getInstance().collection("groups").document(groupId).delete();
                Intent intent = new Intent(GroupProfile.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

            }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });

        findViewById(R.id.leave).setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfile.this);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure to leave this group ?");
            builder.setPositiveButton("Delete", (dialog, which) -> FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Participants").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            if (snapshot1.exists()){

                                String stamp = ""+System.currentTimeMillis();
                                HashMap<String, Object> hashMap2 = new HashMap<>();
                                hashMap2.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                                hashMap2.put("msg", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()+" has left the group");
                                hashMap2.put("type", "add");
                                hashMap2.put("timestamp", stamp);
                                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                                        .setValue(hashMap2);

                                snapshot1.getRef().removeValue();
                                Intent intent = new Intent(GroupProfile.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                Toast.makeText(GroupProfile.this, "Leaved", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    })).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });

        findViewById(R.id.add).setOnClickListener(view -> {
            Intent i = new Intent(this, AddMembersActivity.class);
            i.putExtra("group", groupId);
            startActivity(i);
        });

        findViewById(R.id.menu).setOnClickListener(view -> {
            Intent i = new Intent(this, AddMembersActivity.class);
            i.putExtra("group", groupId);
            startActivity(i);
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getUser() {
        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            userList.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                for (String no : list){
                    if (no.equals(Objects.requireNonNull(ds.get("id")).toString())){
                        userList.add(ds.toObject(UserModel.class));
                    }
                }
                adapterUsers = new AdapterParticipants(this, userList, ""+groupId,myRole);
                users_rv.setAdapter(adapterUsers);
                adapterUsers.notifyDataSetChanged();
            }
        });
    }
}