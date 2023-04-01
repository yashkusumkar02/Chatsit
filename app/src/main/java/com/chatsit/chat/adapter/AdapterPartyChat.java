package com.chatsit.chat.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.R;
import com.chatsit.chat.model.ModelPartyChat;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class AdapterPartyChat extends RecyclerView.Adapter<AdapterPartyChat.MyHolder>{

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    final Context context;
    final List<ModelPartyChat> modelPartyChat;

    public AdapterPartyChat(Context context, List<ModelPartyChat> modelPartyChat) {
        this.context = context;
        this.modelPartyChat = modelPartyChat;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.party_chat_right, parent, false);

            return new MyHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.party_chat_left, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String hisUID = modelPartyChat.get(position).getUserId();
        String message = modelPartyChat.get(position).getMsg();
        holder.msg.setText(message);

        FirebaseFirestore.getInstance().collection("users").document(hisUID).addSnapshotListener((value, error) -> {

            assert value != null;
            holder.story_username.setText(Objects.requireNonNull(value.get("name")).toString());

            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(holder.live_photo);
            }

        });

    }


    @Override
    public int getItemCount() {
        return modelPartyChat.size();
    }


    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView live_photo;
        final TextView story_username;
        final TextView msg;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            live_photo = itemView.findViewById(R.id.dp);
            story_username = itemView.findViewById(R.id.username);
            msg = itemView.findViewById(R.id.msg);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (modelPartyChat.get(position).getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_LEFT;
        }
    }

}
