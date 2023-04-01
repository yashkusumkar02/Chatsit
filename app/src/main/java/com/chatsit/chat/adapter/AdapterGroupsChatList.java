package com.chatsit.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.R;
import com.chatsit.chat.group.GroupChatActivity;
import com.chatsit.chat.model.ModelGroups;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterGroupsChatList extends RecyclerView.Adapter<AdapterGroupsChatList.MyHolder>{

    final Context context;
    final List<ModelGroups> userList;

    public AdapterGroupsChatList(Context context, List<ModelGroups> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.chat_list_group, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        if (position>1 && (position+1) % 4 == 0) {
            holder.ad.setVisibility(View.VISIBLE);
        }

        ModelGroups modelChatListGroups = userList.get(position);

        loadLastMsg(holder, modelChatListGroups);


        FirebaseFirestore.getInstance().collection("groups").document(modelChatListGroups.getGroup()).addSnapshotListener((value, error) -> {

            holder.name.setText(Objects.requireNonNull(value.get("name")).toString());

            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(holder.dp);
            }

        });


        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, GroupChatActivity.class);
            intent.putExtra("group", userList.get(position).getGroup());
            context.startActivity(intent);
        });

    }

    private void loadLastMsg(MyHolder holder, ModelGroups modelChatListGroups) {
        FirebaseDatabase.getInstance().getReference().child("Groups").child(modelChatListGroups.getGroup()).child("Message").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot ds: snapshot.getChildren()){
                                String message = ""+ds.child("msg").getValue();
                                String time = ""+ds.child("timestamp").getValue();
                                String sender = ""+ds.child("sender").getValue();
                                String type = ""+ds.child("type").getValue();
                                FirebaseFirestore.getInstance().collection("users").document(sender).addSnapshotListener((value, error) -> {

                                    String name = Objects.requireNonNull(value.get("name")).toString();
                                    switch (type) {
                                        case "image":
                                            holder.username.setText(name + " : " + "Sent a photo");
                                            break;
                                        case "video":
                                            holder.username.setText(name + " : " + "Sent a video");
                                            break;
                                        case "post":
                                            holder.username.setText(name + " : " + "Sent a post");
                                            break;
                                        case "gif":
                                            holder.username.setText(name + " : " + "Sent a GIF");
                                            break;
                                        case "audio":
                                            holder.username.setText(name + " : " + "Sent a audio");
                                        case "doc":
                                            holder.username.setText(name + " : " + "Sent a document");
                                            break;
                                        case "location":
                                            holder.username.setText("Sent a location");
                                            break;
                                        case "party":
                                            holder.username.setText("Sent a party invitation");
                                            break;
                                        case "reply":
                                            holder.username.setText("Has replied");
                                            break;
                                        case "contact":
                                            holder.username.setText("Sent a contact");
                                            break;
                                        case "sticker":
                                            holder.username.setText("Sent a sticker");
                                            break;
                                        case "voice_call":
                                            holder.username.setText("Voice called");
                                            break;
                                        case "video_call":
                                            holder.username.setText("Video called");
                                            break;
                                        case "theme":
                                            holder.username.setText("Theme changed");
                                            break;

                                        case "add":
                                            holder.username.setText(message);
                                            break;

                                        case "reel":
                                            holder.username.setText("Sent a reel");
                                            break;
                                        case "story":
                                        case "high":
                                            holder.username.setText("Sent a story");
                                            break;
                                        default:
                                            holder.username.setText(name + " : " + message);
                                            break;
                                    }

                                });
                                SimpleDateFormat formatter = new SimpleDateFormat("h:mm");
                                String dateString = formatter.format(new Date(Long.parseLong(time)));
                                holder.time.setText(dateString);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final TextView name;
        final TextView username;
        final TextView time;
        final RelativeLayout ad;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.time);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.phone);
            ad = itemView.findViewById(R.id.ad);

            MobileAds.initialize(itemView.getContext(), initializationStatus -> {
            });
            AdLoader.Builder builder = new AdLoader.Builder(itemView.getContext(), itemView.getContext().getString(R.string.native_ad_unit_id));
            builder.forUnifiedNativeAd(unifiedNativeAd -> {
                TemplateView templateView = itemView.findViewById(R.id.my_template);
                templateView.setNativeAd(unifiedNativeAd);
            });

            AdLoader adLoader = builder.build();
            AdRequest adRequest = new AdRequest.Builder().build();
            adLoader.loadAd(adRequest);

        }

    }
}
