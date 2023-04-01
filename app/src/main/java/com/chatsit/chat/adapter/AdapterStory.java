package com.chatsit.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.circularstatusview.CircularStatusView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.GetTimeAgo;
import com.chatsit.chat.R;
import com.chatsit.chat.model.ModelStory;
import com.chatsit.chat.user.StoryViewActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterStory extends RecyclerView.Adapter<AdapterStory.ViewHolder> {

    private final Context context;
    private final List<ModelStory>storyList;

    public AdapterStory(Context context, List<ModelStory> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_item, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        ModelStory story = storyList.get(position);
        userInfo(viewHolder, story.getUserid(), position);

        seenStory(viewHolder, story.getUserid());

        viewHolder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StoryViewActivity.class);
            intent.putExtra("userid", story.getUserid());
            context.startActivity(intent);
        });

        viewHolder.phone.setText(GetTimeAgo.getTimeAgo(story.getTimestart()));

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").orderByChild("id").equalTo(storyList.get(position).getUserid()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
                                ViewGroup.LayoutParams params = viewHolder.itemView.getLayoutParams();
                                params.height = 0;
                                viewHolder.itemView.setLayoutParams(params);
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
        return storyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        final CircularStatusView circularStatusView;
        final CircleImageView dp;
        final TextView name;
        final TextView phone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circularStatusView =  itemView.findViewById(R.id.circular_status_view);
            dp =  itemView.findViewById(R.id.dp);
            name =  itemView.findViewById(R.id.name);
            phone =  itemView.findViewById(R.id.phone);
        }
    }

    private void userInfo (ViewHolder viewHolder, String userId, int pos){


        FirebaseFirestore.getInstance().collection("users").document(userId).addSnapshotListener((value, error) -> {
            viewHolder.name.setText(Objects.requireNonNull(value.get("name")).toString());
            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(viewHolder.dp);
            }

        });


    }


    private void seenStory(ViewHolder viewHolder, String userId){
     FirebaseDatabase.getInstance().getReference("Story")
                .child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
           if (!snapshot1.child("views").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists() && System.currentTimeMillis() < Objects.requireNonNull(snapshot1.getValue(ModelStory.class)).getTimeend()){
               i++;
           }
                }
                if (i > 0){
                    //New
                    viewHolder.circularStatusView.setPortionsCount(i);
                }else {
                    //Seen
                    viewHolder.circularStatusView.setPortionsCount(i);
                    viewHolder.circularStatusView.setPortionsColor(context.getResources().getColor(R.color.indicator));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
