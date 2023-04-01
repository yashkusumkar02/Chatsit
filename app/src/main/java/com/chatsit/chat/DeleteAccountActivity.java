package com.chatsit.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.auth.GenerateOTPActivity;

import java.util.Objects;

public class DeleteAccountActivity extends AppCompatActivity {

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        //ChatList
        FirebaseDatabase.getInstance().getReference().child("Chatlist").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(Objects.requireNonNull(ds.getKey())).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
                }
                FirebaseDatabase.getInstance().getReference().child("Chatlist").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Chat
        FirebaseDatabase.getInstance().getReference().child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (Objects.requireNonNull(ds.child("sender").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        FirebaseDatabase.getInstance().getReference().child("Chats").child(Objects.requireNonNull(ds.getKey())).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (Objects.requireNonNull(ds.child("receiver").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        FirebaseDatabase.getInstance().getReference().child("Chats").child(Objects.requireNonNull(ds.getKey())).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("calling").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    if (Objects.requireNonNull(ds.child("from").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) || Objects.requireNonNull(ds.child("to").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        FirebaseDatabase.getInstance().getReference().child("calling").child(Objects.requireNonNull(ds.getKey())).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Group
        FirebaseDatabase.getInstance().getReference().child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){

                    FirebaseDatabase.getInstance().getReference().child("groups").child(Objects.requireNonNull(ds.getKey())).child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                snapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child("groups").child(ds.getKey()).child("Message").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                if (Objects.requireNonNull(ds.child("sender").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    FirebaseDatabase.getInstance().getReference("groups").child(Objects.requireNonNull(ds.getKey())).child("Message").child(ds.getKey()).getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //Token
        FirebaseDatabase.getInstance().getReference().child("Tokens").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
        //Users
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getRef().removeValue();
        //FireStore
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();
        FirebaseFirestore.getInstance().collection("privacy").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete().addOnCompleteListener(task -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DeleteAccountActivity.this, GenerateOTPActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}