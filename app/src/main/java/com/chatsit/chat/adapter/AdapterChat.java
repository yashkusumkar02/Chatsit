package com.chatsit.chat.adapter;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.StaticMapCriteria;
import com.mapbox.geojson.Point;
import com.nguyencse.URLEmbeddedView;
import com.chatsit.chat.MediaViewActivity;
import com.chatsit.chat.R;
import com.chatsit.chat.model.ChatModel;
import com.chatsit.chat.party.StartPartyActivity;
import com.chatsit.chat.party.StartYouTubeActivity;
import com.chatsit.chat.user.ChatActivity;
import com.chatsit.chat.user.MeetingActivity;
import com.chatsit.chat.user.SendToActivity;
import com.squareup.picasso.Picasso;
import com.tylersuehr.socialtextview.SocialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

@SuppressWarnings("ALL")
public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    final Context context;
    final List<ChatModel> createModels;
    BottomSheetDialog chat_more;
    @SuppressWarnings("unused")
    boolean MSG_TYPE_LEFT = false;
    boolean MSG_TYPE_RIGHT = false;

    public static List<String> extractUrls(String text)
    {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find())
        {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }


    public AdapterChat(Context context, List<ChatModel> createModels) {
        this.context = context;
        this.createModels = createModels;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_right, parent, false);
            return new AdapterChat.MyHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.chat_left, parent, false);
        return new AdapterChat.MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        if (position == createModels.size()-1){
            FirebaseDatabase.getInstance().getReference().child("Chats").orderByChild("timestamp").equalTo(createModels.get(position).getTimestamp()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds : snapshot.getChildren()){
                        if (Boolean.parseBoolean(Objects.requireNonNull(ds.child("isSeen").getValue()).toString()) == true){
                            holder.seen.setText("Seen");
                        }else{
                            holder.seen.setText("Delivered");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else {
            holder.seen.setVisibility(View.GONE);
        }

        switch (createModels.get(position).getType()){

            case "text":

                List<String> extractedUrls = extractUrls(createModels.get(position).getMsg());

                for (String url : extractedUrls)
                {
                    holder.urlEmbeddedView.setVisibility(View.VISIBLE);

                    holder.urlEmbeddedView.setURL(url, data -> {
                        holder.urlEmbeddedView.title(data.getTitle());
                        holder.urlEmbeddedView.description(data.getDescription());
                        holder.urlEmbeddedView.host(data.getHost());
                        holder.urlEmbeddedView.thumbnail(data.getThumbnailURL());
                        holder.urlEmbeddedView.favor(data.getFavorURL());
                    });
                }

                holder.msg.setVisibility(View.VISIBLE);
                holder.msg.setLinkText(createModels.get(position).getMsg());
                holder.msg.setOnLinkClickListener((i, s) -> {
                    if (i == 16){
                        if (!s.startsWith("https://") && !s.startsWith("http://")){
                            s = "http://" + s;
                        }
                        Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                        context.startActivity(openUrlIntent);
                    }else if (i == 4){
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                        context.startActivity(intent);
                    }else if (i == 8){
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, s);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        context.startActivity(intent);
                    }
                });
                break;
            case "reply":

                FirebaseDatabase.getInstance().getReference().child("Reply").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.reply_text.setVisibility(View.VISIBLE);
                        holder.reply_text.setText(Objects.requireNonNull(snapshot.child("msg").getValue()).toString());
                        holder.msg.setVisibility(View.VISIBLE);
                        holder.msg.setLinkText(Objects.requireNonNull(snapshot.child("reply").getValue()).toString());
                        holder.msg.setOnLinkClickListener((i, s) -> {
                            if (i == 16){
                                if (!s.startsWith("https://") && !s.startsWith("http://")){
                                    s = "http://" + s;
                                }
                                Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                                context.startActivity(openUrlIntent);
                            }else if (i == 4){
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", s, null));
                                context.startActivity(intent);
                            }else if (i == 8){
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:"));
                                intent.putExtra(Intent.EXTRA_EMAIL, s);
                                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                                context.startActivity(intent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                break;
            case "image":
                holder.media_layout.setVisibility(View.VISIBLE);
                holder.media.setVisibility(View.VISIBLE);
                Picasso.get().load(createModels.get(position).getMsg()).into(holder.media);
                break;
            case "video":
                holder.media_layout.setVisibility(View.VISIBLE);
                holder.media.setVisibility(View.VISIBLE);
                holder.play.setVisibility(View.VISIBLE);
                Glide.with(context).asBitmap().load(createModels.get(position).getMsg()).thumbnail(0.1f).into(holder.media);
                break;
            case "sticker":
                holder.media_layout.setVisibility(View.VISIBLE);
                holder.media.setVisibility(View.VISIBLE);
                Glide.with(context).load(createModels.get(position).getMsg()).thumbnail(0.1f).into(holder.media);
                break;
            case "story":
                holder.media_layout.setVisibility(View.VISIBLE);
                holder.media.setVisibility(View.VISIBLE);
                holder.msg.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference().child("ChatStory").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        holder.msg.setLinkText(Objects.requireNonNull(snapshot.child("msg").getValue()).toString());
                        FirebaseDatabase.getInstance().getReference("Story")
                                .child(createModels.get(position).getReceiver()).child(Objects.requireNonNull(snapshot.child("story").getValue()).toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("image")){
                                    Picasso.get().load(Objects.requireNonNull(snapshot.child("imageUri").getValue()).toString()).into(holder.media);
                                }else {
                                    Glide.with(context).asBitmap().load(Objects.requireNonNull(snapshot.child("imageUri").getValue()).toString()).thumbnail(0.1f).into(holder.media);
                                    Toast.makeText(context, Objects.requireNonNull(snapshot.child("imageUri").getValue()).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case "gif":
                holder.media_layout.setVisibility(View.VISIBLE);
                holder.media.setVisibility(View.VISIBLE);
                Glide.with(context).load(createModels.get(position).getMsg()).into(holder.media);
                break;
            case "location":
                FirebaseDatabase.getInstance().getReference().child("Location").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("long").getValue()).toString());
                            double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("lat").getValue()).toString());

                            MapboxStaticMap staticImage = MapboxStaticMap.builder()
                                    .accessToken(context.getResources().getString(R.string.map_box))
                                    .styleId(StaticMapCriteria.DARK_STYLE)
                                    .cameraPoint(Point.fromLngLat(longitude, latitude))
                                    .cameraZoom(13)
                                    .width(250)
                                    .height(200)
                                    .retina(true)
                                    .build();

                            holder.media_layout.setVisibility(View.VISIBLE);
                            holder.media.setVisibility(View.VISIBLE);
                            String imageUrl = staticImage.url().toString();
                            Picasso.get().load(imageUrl).into(holder.media);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case "audio":
                holder.voicePlayerView.setVisibility(View.VISIBLE);
                holder.voicePlayerView.setAudio(createModels.get(position).getMsg());
                break;
            case "doc":
             holder.document.setVisibility(View.VISIBLE);
             FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg()).getMetadata().addOnSuccessListener(storageMetadata -> holder.title.setText(storageMetadata.getName()));
             break;
            case "party":
                holder.party.setVisibility(View.VISIBLE);
                break;
            case "theme":
                holder.theme_ly.setVisibility(View.VISIBLE);
                break;
            case "voice":
                holder.call_y.setVisibility(View.VISIBLE);
                holder.call_text.setVisibility(View.VISIBLE);
                holder.call_text.setText("Voice called");
                break;
            case "video_call":
                holder.video_ly.setVisibility(View.VISIBLE);
                holder.video_text.setVisibility(View.VISIBLE);
                holder.video_text.setText("video called");
                break;
            case "meet":
                holder.meeting.setVisibility(View.VISIBLE);
                break;
            case "contact":
                holder.contacts.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            holder.namePhone.setText(Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                            holder.noPhone.setText(Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
        }




        holder.itemView.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.msg.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.media_layout.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.voicePlayerView.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.document.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.party.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.meeting.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.contacts.setOnLongClickListener(view -> {

            addAttachment(holder,position);
            chat_more.show();

            return true;
        });

        holder.itemView.setOnClickListener(v -> {

            switch (createModels.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });

                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "contact":

                    String[] colors = {"Add contact", "Start chat",};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Pick a action");
                    builder.setItems(colors, (dialog, which) -> {
                        if (which == 0){
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.NAME, Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        context.startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Query userPhone = FirebaseFirestore.getInstance().collection("users").whereEqualTo("phone", Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        userPhone.addSnapshotListener((value, error) -> {
                                            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                                                if (ds != null){
                                                    Intent intent1 = new Intent(context, ChatActivity.class);
                                                    intent1.putExtra("id", Objects.requireNonNull(ds.get("id")).toString());
                                                    context.startActivity(intent1);
                                                }else {
                                                    Snackbar.make(v, "User doesn't exist", Snackbar.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    builder.show();

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild("room")){


                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);

                                String timeStamp = "" + System.currentTimeMillis();
                                HashMap<String, Object> hashMap1 = new HashMap<>();
                                hashMap1.put("ChatId", timeStamp);
                                hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap1.put("msg", "has joined");
                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")) {
                                    Intent intent = new Intent(context, StartYouTubeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, StartPartyActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                }

                            }else {
                                Snackbar.make(holder.itemView, "Party doesn't exist", Snackbar.LENGTH_LONG).show();
                                FirebaseDatabase.getInstance().getReference("Chats").child(createModels.get(position).getTimestamp()).getRef().removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("long").getValue()).toString());
                                double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("lat").getValue()).toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", createModels.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }
        });

        holder.contacts.setOnClickListener(v -> {

            switch (createModels.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });

                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "contact":

                    String[] colors = {"Add contact", "Start chat",};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Pick a action");
                    builder.setItems(colors, (dialog, which) -> {
                        if (which == 0){
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.NAME, Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        context.startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Query userPhone = FirebaseFirestore.getInstance().collection("users").whereEqualTo("phone", Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        userPhone.addSnapshotListener((value, error) -> {
                                            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                                                if (ds != null){
                                                    Intent intent1 = new Intent(context, ChatActivity.class);
                                                    intent1.putExtra("id", Objects.requireNonNull(ds.get("id")).toString());
                                                    context.startActivity(intent1);
                                                }else {
                                                    Snackbar.make(v, "User doesn't exist", Snackbar.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    builder.show();

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild("room")){


                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);

                                String timeStamp = "" + System.currentTimeMillis();
                                HashMap<String, Object> hashMap1 = new HashMap<>();
                                hashMap1.put("ChatId", timeStamp);
                                hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap1.put("msg", "has joined");
                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")) {
                                    Intent intent = new Intent(context, StartYouTubeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, StartPartyActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                }

                            }else {
                                Snackbar.make(holder.itemView, "Party doesn't exist", Snackbar.LENGTH_LONG).show();
                                FirebaseDatabase.getInstance().getReference("Chats").child(createModels.get(position).getTimestamp()).getRef().removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("long").getValue()).toString());
                                double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("lat").getValue()).toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", createModels.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }
        });

        holder.meeting.setOnClickListener(v -> {

            switch (createModels.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });

                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "contact":

                    String[] colors = {"Add contact", "Start chat",};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Pick a action");
                    builder.setItems(colors, (dialog, which) -> {
                        if (which == 0){
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.NAME, Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        context.startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Query userPhone = FirebaseFirestore.getInstance().collection("users").whereEqualTo("phone", Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        userPhone.addSnapshotListener((value, error) -> {
                                            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                                                if (ds != null){
                                                    Intent intent1 = new Intent(context, ChatActivity.class);
                                                    intent1.putExtra("id", Objects.requireNonNull(ds.get("id")).toString());
                                                    context.startActivity(intent1);
                                                }else {
                                                    Snackbar.make(v, "User doesn't exist", Snackbar.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    builder.show();

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild("room")){


                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);

                                String timeStamp = "" + System.currentTimeMillis();
                                HashMap<String, Object> hashMap1 = new HashMap<>();
                                hashMap1.put("ChatId", timeStamp);
                                hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap1.put("msg", "has joined");
                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")) {
                                    Intent intent = new Intent(context, StartYouTubeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, StartPartyActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                }

                            }else {
                                Snackbar.make(holder.itemView, "Party doesn't exist", Snackbar.LENGTH_LONG).show();
                                FirebaseDatabase.getInstance().getReference("Chats").child(createModels.get(position).getTimestamp()).getRef().removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("long").getValue()).toString());
                                double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("lat").getValue()).toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", createModels.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }
        });

        holder.party.setOnClickListener(v -> {

            switch (createModels.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });

                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "contact":

                    String[] colors = {"Add contact", "Start chat",};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Pick a action");
                    builder.setItems(colors, (dialog, which) -> {
                        if (which == 0){
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.NAME, Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        context.startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Query userPhone = FirebaseFirestore.getInstance().collection("users").whereEqualTo("phone", Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        userPhone.addSnapshotListener((value, error) -> {
                                            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                                                if (ds != null){
                                                    Intent intent1 = new Intent(context, ChatActivity.class);
                                                    intent1.putExtra("id", Objects.requireNonNull(ds.get("id")).toString());
                                                    context.startActivity(intent1);
                                                }else {
                                                    Snackbar.make(v, "User doesn't exist", Snackbar.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    builder.show();

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild("room")){


                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);

                                String timeStamp = "" + System.currentTimeMillis();
                                HashMap<String, Object> hashMap1 = new HashMap<>();
                                hashMap1.put("ChatId", timeStamp);
                                hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap1.put("msg", "has joined");
                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")) {
                                    Intent intent = new Intent(context, StartYouTubeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, StartPartyActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                }

                            }else {
                                Snackbar.make(holder.itemView, "Party doesn't exist", Snackbar.LENGTH_LONG).show();
                                FirebaseDatabase.getInstance().getReference("Chats").child(createModels.get(position).getTimestamp()).getRef().removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("long").getValue()).toString());
                                double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("lat").getValue()).toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", createModels.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }
        });

        holder.document.setOnClickListener(v -> {

            switch (createModels.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });

                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "contact":

                    String[] colors = {"Add contact", "Start chat",};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Pick a action");
                    builder.setItems(colors, (dialog, which) -> {
                        if (which == 0){
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.NAME, Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        context.startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Query userPhone = FirebaseFirestore.getInstance().collection("users").whereEqualTo("phone", Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        userPhone.addSnapshotListener((value, error) -> {
                                            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                                                if (ds != null){
                                                    Intent intent1 = new Intent(context, ChatActivity.class);
                                                    intent1.putExtra("id", Objects.requireNonNull(ds.get("id")).toString());
                                                    context.startActivity(intent1);
                                                }else {
                                                    Snackbar.make(v, "User doesn't exist", Snackbar.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    builder.show();

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild("room")){


                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);

                                String timeStamp = "" + System.currentTimeMillis();
                                HashMap<String, Object> hashMap1 = new HashMap<>();
                                hashMap1.put("ChatId", timeStamp);
                                hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap1.put("msg", "has joined");
                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")) {
                                    Intent intent = new Intent(context, StartYouTubeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, StartPartyActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                }

                            }else {
                                Snackbar.make(holder.itemView, "Party doesn't exist", Snackbar.LENGTH_LONG).show();
                                FirebaseDatabase.getInstance().getReference("Chats").child(createModels.get(position).getTimestamp()).getRef().removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("long").getValue()).toString());
                                double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("lat").getValue()).toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", createModels.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }
        });

        holder.media_layout.setOnClickListener(v -> {

            switch (createModels.get(position).getType()) {

                case "doc":

                    Snackbar.make(v, "Downloading...", Snackbar.LENGTH_LONG).show();

                    StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg());
                    picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                        picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                            String extension = storageMetadata.getContentType();
                            String url = uri.toString();
                            downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                        });


                    });

                    break;

                case "image":

                    Intent intent = new Intent(context, MediaViewActivity.class);
                    intent.putExtra("type", "image");
                    intent.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent);

                    break;
                case "video":

                    Intent intent1 = new Intent(context, MediaViewActivity.class);
                    intent1.putExtra("type", "video");
                    intent1.putExtra("uri", createModels.get(position).getMsg());
                    context.startActivity(intent1);

                    break;
                case "contact":

                    String[] colors = {"Add contact", "Start chat",};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Pick a action");
                    builder.setItems(colors, (dialog, which) -> {
                        if (which == 0){
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.NAME, Objects.requireNonNull(snapshot.child("name").getValue()).toString());
                                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        context.startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            FirebaseDatabase.getInstance().getReference().child("Contact").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        Query userPhone = FirebaseFirestore.getInstance().collection("users").whereEqualTo("phone", Objects.requireNonNull(snapshot.child("number").getValue()).toString());
                                        userPhone.addSnapshotListener((value, error) -> {
                                            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                                                if (ds != null){
                                                    Intent intent1 = new Intent(context, ChatActivity.class);
                                                    intent1.putExtra("id", Objects.requireNonNull(ds.get("id")).toString());
                                                    context.startActivity(intent1);
                                                }else {
                                                    Snackbar.make(v, "User doesn't exist", Snackbar.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                    builder.show();

                    break;
                case "party":

                    FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.hasChild("room")){


                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).setValue(true);

                                String timeStamp = "" + System.currentTimeMillis();
                                HashMap<String, Object> hashMap1 = new HashMap<>();
                                hashMap1.put("ChatId", timeStamp);
                                hashMap1.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                hashMap1.put("msg", "has joined");
                                FirebaseDatabase.getInstance().getReference().child("Party").child(createModels.get(position).getTimestamp()).child("Chats").child(timeStamp).setValue(hashMap1);

                                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")) {
                                    Intent intent = new Intent(context, StartYouTubeActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, StartPartyActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("room", createModels.get(position).getTimestamp());
                                    context.startActivity(intent);
                                }

                            }else {
                                Snackbar.make(holder.itemView, "Party doesn't exist", Snackbar.LENGTH_LONG).show();
                                FirebaseDatabase.getInstance().getReference("Chats").child(createModels.get(position).getTimestamp()).getRef().removeValue();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;

                case "location":

                    FirebaseDatabase.getInstance().getReference().child("Location").child(createModels.get(position).getMsg()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                double longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("long").getValue()).toString());
                                double latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("lat").getValue()).toString());

                                String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                                Intent intent11 = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent11);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    break;
                case "meet":

                    Intent intent21 = new Intent(context, MeetingActivity.class);
                    intent21.putExtra("meet", createModels.get(position).getTimestamp());
                    context.startActivity(intent21);

                    break;

            }
        });


    }



    private void addAttachment(MyHolder holder, int position) {
        if (chat_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.chat_bottom, null);

             view.findViewById(R.id.delete).setOnClickListener(view1 -> {
                 chat_more.dismiss();
                 FirebaseDatabase.getInstance().getReference().child("Chats").addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         for (DataSnapshot ds : snapshot.getChildren()){
                             if (Objects.requireNonNull(ds.child("timestamp").getValue()).toString().equals(createModels.get(position).getTimestamp())){
                                 ds.getRef().removeValue();
                                 ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                                 params.height = 0;
                                 holder.itemView.setLayoutParams(params);
                             }
                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {

                     }
                 });
             });

             view.findViewById(R.id.share).setOnClickListener(view1 -> {
                 chat_more.dismiss();
                 if (createModels.get(position).getType().equals("text")){
                     shareText(createModels.get(position).getMsg());
                 }else  if (createModels.get(position).getType().equals("image")){
                     shareImage(Uri.parse(createModels.get(position).getMsg()));
                 }else {
                     Intent shareIntent = new Intent(Intent.ACTION_SEND);
                     shareIntent.setType("text/plain");
                     shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chats");
                     shareIntent.putExtra(Intent.EXTRA_TEXT, createModels.get(position).getType());
                     context.startActivity(Intent.createChooser(shareIntent, "Share Via"));
                 }
             });

             view.findViewById(R.id.copy).setOnClickListener(view1 -> {
                 chat_more.dismiss();
                 ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                 ClipData clip = ClipData.newPlainText("text", createModels.get(position).getMsg());
                 clipboard.setPrimaryClip(clip);
                 Snackbar.make(view1, "Copied", Snackbar.LENGTH_SHORT).show();
             });

             if (createModels.get(position).getType().equals("video") || createModels.get(position).getType().equals("image")){
                 view.findViewById(R.id.dy).setVisibility(View.VISIBLE);
             }else {
                 view.findViewById(R.id.dy).setVisibility(View.GONE);
             }

            view.findViewById(R.id.download).setOnClickListener(view1 -> {
                chat_more.dismiss();
                Snackbar.make(view1, "Downloading...", Snackbar.LENGTH_LONG).show();

                StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(createModels.get(position).getMsg());
                picRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    picRef.getMetadata().addOnSuccessListener(storageMetadata -> {
                        String extension = storageMetadata.getContentType();
                        String url = uri.toString();
                        downloadDoc(context, DIRECTORY_DOWNLOADS, url, extension);
                    });


                });
            });

            view.findViewById(R.id.mic).setOnClickListener(view1 -> {
                Intent i = new Intent(context, SendToActivity.class);
                i.putExtra("uri", createModels.get(position).getMsg());
                i.putExtra("type", createModels.get(position).getType());
                context.startActivity(i);
                chat_more.dismiss();
            });

            view.findViewById(R.id.reply).setOnClickListener(view1 -> {

                if (createModels.get(position).getMsg().equals("reply")){
                    Toast.makeText(context, "You cant reply on reply", Toast.LENGTH_SHORT).show();
                }else {
                    String text = createModels.get(position).getMsg();
                    Intent intent = new Intent("custom-message");
                    intent.putExtra("text",text);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
                chat_more.dismiss();
            });

            chat_more = new BottomSheetDialog(context);
            chat_more.setContentView(view);
        }
    }

    private void downloadDoc(Context context, String directoryDownloads, String url, String extension) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri1 = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri1);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, directoryDownloads, extension);
        Objects.requireNonNull(downloadManager).enqueue(request);
    }


    @Override
    public int getItemCount() {
        return createModels.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final SocialTextView msg;
        final CardView media_layout;
        final ImageView play;
        final ImageView media;
        final VoicePlayerView voicePlayerView;
        final LinearLayout document;
        final LinearLayout party;
        final LinearLayout meeting;
        final LinearLayout contacts;
        final TextView title;
        final TextView namePhone;
        final TextView noPhone;
        final TextView reply_text;
        final URLEmbeddedView urlEmbeddedView;
         final TextView seen;
         final LinearLayout video_ly;
        final LinearLayout call_y;
        final LinearLayout theme_ly;
         final TextView call_text;
        final TextView video_text;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            theme_ly =  itemView.findViewById(R.id.theme_ly);
            call_text=  itemView.findViewById(R.id.call_text);
            video_text=  itemView.findViewById(R.id.video_text);
            video_ly=  itemView.findViewById(R.id.video_ly);
            call_y=  itemView.findViewById(R.id.call_y);
            seen   =  itemView.findViewById(R.id.seen);
            urlEmbeddedView = itemView.findViewById(R.id.uev);
            reply_text = itemView.findViewById(R.id.reply_text);
            msg = itemView.findViewById(R.id.text);
            media_layout =  itemView.findViewById(R.id.media_layout);
            play = itemView.findViewById(R.id.play);
            media =  itemView.findViewById(R.id.media);
            voicePlayerView  =  itemView.findViewById(R.id.voicePlayerView);
            document =  itemView.findViewById(R.id.document);
            title =  itemView.findViewById(R.id.title);
            party = itemView.findViewById(R.id.party);
            meeting = itemView.findViewById(R.id.meeting);
            contacts = itemView.findViewById(R.id.contacts);
            noPhone = itemView.findViewById(R.id.noPhone);
            namePhone = itemView.findViewById(R.id.namePhone);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (createModels.get(position).getSender().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
            MSG_TYPE_RIGHT = true;
            MSG_TYPE_LEFT = false;
        }else {
            MSG_TYPE_RIGHT = false;
            MSG_TYPE_LEFT = true;
        }
        return position;
    }

    private void shareText(String text){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chats");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"));

    }

    private void shareImage(Uri uri){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chats");
        shareIntent.putExtra(Intent.EXTRA_STREAM, getContentUri(uri));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share Via"));

    }

    private Uri getContentUri(Uri uri){
        Bitmap bitmap = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }else {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File imagesFolder = new File(context.getCacheDir(), "images");
        Uri contentUri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.PNG, 50, stream);
            stream.flush();
            stream.close();
            contentUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName()+".fileprovider", file);

        }
        catch (Exception e){

        }

        return contentUri;
    }

}
