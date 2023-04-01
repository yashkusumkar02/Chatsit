package com.chatsit.chat.party;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterPartyChat;
import com.chatsit.chat.model.ModelPartyChat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PartyChatActivity extends DialogFragment {

    public static PartyChatActivity newInstance() {
        return new PartyChatActivity();
    }

    CallBack callBack;
    EditText sendMessage;
    ImageView send;
    RecyclerView chat_rv;

    //Post
    AdapterPartyChat partyChat;
    List<ModelPartyChat> modelPartyChats;

    public void setCallBack(CallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_party_chat, container, false);

        //back
        v.findViewById(R.id.imageView).setOnClickListener(v1 -> {
            callBack.onActionClick("type", "sent");
            dismiss();
        });

        //Id
        sendMessage = v.findViewById(R.id.editText);
        send = v.findViewById(R.id.message_send);
        chat_rv = v.findViewById(R.id.chat_rv);

        modelPartyChats = new ArrayList<>();

        chat_rv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
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
                FirebaseDatabase.getInstance().getReference().child("Party").child(WatchPartyActivity.getId()).child("Chats").child(timeStamp).setValue(hashMap);

                sendMessage.setText("");

            }
        });

        readMessage();

        return v;
    }

    private void readMessage() {
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference().child("Party").child(WatchPartyActivity.getId()).child("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelPartyChats.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPartyChat modelLiveChat = ds.getValue(ModelPartyChat.class);
                    modelPartyChats.add(modelLiveChat);
                }
                partyChat = new AdapterPartyChat(getActivity(), modelPartyChats);
                chat_rv.setAdapter(partyChat);
                partyChat.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public interface CallBack {
        void onActionClick(String type, String value);
    }

}
