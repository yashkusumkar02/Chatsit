package com.chatsit.chat.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterGroupsChatList;
import com.chatsit.chat.group.CreateGroupActivity;
import com.chatsit.chat.model.ModelGroups;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class GroupFragment extends Fragment {

    AdapterGroupsChatList getAdapterGroups;
    List<ModelGroups> modelGroupsList;
    RecyclerView groups_chat;
    LinearLayout nothing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_group, container, false);

        nothing = v.findViewById(R.id.nothing);

        v.findViewById(R.id.floating_action_button).setOnClickListener(view -> {
            startActivity(new Intent(getContext(), CreateGroupActivity.class));
        });

        groups_chat = v.findViewById(R.id.groups_chat);
        groups_chat.setLayoutManager(new LinearLayoutManager(getContext()));
        modelGroupsList = new ArrayList<>();

        AsyncTask.execute(this::getChats);

        return v;
    }

    private void getChats() {
        FirebaseDatabase.getInstance().getReference("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroupsList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Participants").hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        ModelGroups modelChatListGroups = ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(ModelGroups.class);
                        modelGroupsList.add(modelChatListGroups);
                    }
                    getAdapterGroups = new AdapterGroupsChatList(getContext(), modelGroupsList);
                    groups_chat.setAdapter(getAdapterGroups);
                    if (getAdapterGroups.getItemCount() == 0){
                        groups_chat.setVisibility(View.GONE);
                        nothing.setVisibility(View.VISIBLE);
                    }else {
                        groups_chat.setVisibility(View.VISIBLE);
                        nothing.setVisibility(View.GONE);
                    }
                }

                if (!dataSnapshot.exists()){
                    groups_chat.setVisibility(View.GONE);
                    nothing.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}