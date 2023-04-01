package com.chatsit.chat.user;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterCreateChat;
import com.chatsit.chat.model.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewedStoryActivity extends AppCompatActivity {

    //User
    String id;
    private RecyclerView users_rv;
    private List<UserModel> userList;
    private AdapterCreateChat adapterUsers;
    List<String> idList;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who);

        id = getIntent().getStringExtra("id");

        //Back
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        //User
        users_rv = findViewById(R.id.list);
        users_rv.setLayoutManager(new LinearLayoutManager(ViewedStoryActivity.this));
        userList = new ArrayList<>();
        idList = new ArrayList<>();
        getViews();

        //EdiText
        EditText editText = findViewById(R.id.editText);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(editText.getText().toString());
                return true;
            }
            return false;
        });

    }

    private void filter(String query) {

        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            userList.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                for (String no : idList){
                    if (Objects.requireNonNull(ds.get("id")).toString().equals(no)){
                        if (Objects.requireNonNull(ds.get("name")).toString().toLowerCase().contains(query.toLowerCase())){
                            userList.add(ds.toObject(UserModel.class));
                        }
                        break;
                    }
                }
                adapterUsers = new AdapterCreateChat(ViewedStoryActivity.this, userList);
                users_rv.setAdapter(adapterUsers);
                if (adapterUsers.getItemCount() == 0){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.VISIBLE);
                    findViewById(R.id.nothing).setVisibility(View.GONE);
                }
            }
        });

    }

    private void getViews(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(id).child(Objects.requireNonNull(getIntent().getStringExtra("storyid"))).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                idList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    idList.add(snapshot1.getKey());
                }
                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers() {


        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            userList.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                for (String no : idList){
                    if (Objects.requireNonNull(ds.get("id")).toString().equals(no)){
                        userList.add(ds.toObject(UserModel.class));
                        break;
                    }
                }
                adapterUsers = new AdapterCreateChat(ViewedStoryActivity.this, userList);
                users_rv.setAdapter(adapterUsers);
                if (adapterUsers.getItemCount() == 0){
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.GONE);
                    findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    users_rv.setVisibility(View.VISIBLE);
                    findViewById(R.id.nothing).setVisibility(View.GONE);
                }
            }
        });


    }

}