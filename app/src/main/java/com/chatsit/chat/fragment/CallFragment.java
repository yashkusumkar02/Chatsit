package com.chatsit.chat.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterCallLogs;
import com.chatsit.chat.model.CallModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CallFragment extends Fragment {

    RecyclerView recyclerView;
    List<CallModel> callModels;
    AdapterCallLogs adapterCallLogs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_call, container, false);

        recyclerView = v.findViewById(R.id.fragment_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        callModels = new ArrayList<>();

        AsyncTask.execute(() -> FirebaseDatabase.getInstance().getReference("calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callModels.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (Objects.requireNonNull(snapshot.child("from").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()) ||
                            Objects.requireNonNull(snapshot.child("to").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        CallModel chat = snapshot.getValue(CallModel.class);
                        callModels.add(chat);
                    }
                    Collections.reverse(callModels);
                    adapterCallLogs = new AdapterCallLogs(getContext(), callModels);
                    recyclerView.setAdapter(adapterCallLogs);
                    adapterCallLogs.notifyDataSetChanged();
                    if (adapterCallLogs.getItemCount() == 0){
                        v.findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }else {
                        v.findViewById(R.id.nothing).setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
                if (!dataSnapshot.exists()){
                    v.findViewById(R.id.nothing).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }));

        return v;
    }
}