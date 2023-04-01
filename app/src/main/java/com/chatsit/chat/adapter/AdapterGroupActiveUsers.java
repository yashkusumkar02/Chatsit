package com.chatsit.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chatsit.chat.R;
import com.chatsit.chat.group.GroupChatActivity;
import com.chatsit.chat.model.UserModel;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGroupActiveUsers extends RecyclerView.Adapter<AdapterGroupActiveUsers.MyHolder>{

    final Context context;
    final List<UserModel> userList;

    public AdapterGroupActiveUsers(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.goup_chat_user_list, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint({"SetTextI18n", "RecyclerView"})
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        MediaPlayer mp = MediaPlayer.create(context, R.raw.typing);

        FirebaseDatabase.getInstance().getReference().child("users").child(userList.get(position).getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("typing").getValue()).toString().equals(GroupChatActivity.getGroupId())){
                    holder.name.setText("Typing");
                    mp.start();
                }else {
                    holder.name.setText(userList.get(position).getName());
                    mp.stop();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!userList.get(position).getPhoto().isEmpty()){
            Picasso.get().load(userList.get(position).getPhoto()).into(holder.dp);
        }

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public long getItemId(int position) {
        return userList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
        }

    }
}
