package com.chatsit.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.R;
import com.chatsit.chat.model.ChatModel;
import com.chatsit.chat.model.UserModel;
import com.chatsit.chat.user.ChatActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder>{

    final Context context;
    final List<UserModel> modelChatLists;

    public AdapterChatList(Context context, List<UserModel> modelChatLists) {
        this.context = context;
        this.modelChatLists = modelChatLists;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        if (position>1 && (position+1) % 4 == 0) {
            holder.ad.setVisibility(View.VISIBLE);
        }


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").orderByChild("id").equalTo(modelChatLists.get(position).getId()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (ds.exists()){
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

        //UserInfo
        FirebaseDatabase.getInstance().getReference().child("users").child(modelChatLists.get(position).getId()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Time
                if (Objects.requireNonNull(snapshot.child("last").getValue()).toString().equals("online")) holder.online.setVisibility(View.VISIBLE);

                //Typing
                if (Objects.requireNonNull(snapshot.child("typing").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                    holder.message.setText("Typing...");
                }else {
                    //LastMessage
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
                    reference.addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            holder.message.setText("No Message");
                            for (DataSnapshot ds: snapshot.getChildren()){
                                ChatModel chat = ds.getValue(ChatModel.class);
                                if (chat == null){
                                    continue;
                                }
                                String sender = chat.getSender();
                                String receiver = chat.getReceiver();
                                if(sender == null || receiver == null){
                                    continue;
                                }
                                if (chat.getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && chat.getSender().equals(modelChatLists.get(position).getId()) || chat.getReceiver().equals(modelChatLists.get(position).getId()) && chat.getSender().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    switch (chat.getType()) {
                                        case "image":
                                            holder.message.setText("Sent a photo");
                                            break;
                                        case "video":
                                            holder.message.setText("Sent a video");
                                            break;
                                        case "gif":
                                            holder.message.setText("Sent a GIF");
                                            break;
                                        case "sticker":
                                            holder.message.setText("Sent a sticker");
                                            break;
                                        case "audio":
                                            holder.message.setText("Sent a audio");
                                        case "doc":
                                            holder.message.setText("Sent a document");
                                            break;
                                        case "reply":
                                            holder.message.setText("Has replied");
                                            break;
                                        case "voice":
                                            holder.message.setText("Voice called");
                                            break;
                                        case "video_call":
                                            holder.message.setText("Video Called");
                                            break;
                                        case "theme":
                                            holder.message.setText("Chnaged theme");
                                            break;
                                        case "location":
                                            holder.message.setText("Sent a location");
                                            break;
                                        case "contact":
                                            holder.message.setText("Sent a contact");
                                            break;
                                        case "party":
                                            holder.message.setText("Sent a party invitation");
                                            break;
                                        case "story":
                                            holder.message.setText("Sent a story");
                                            break;
                                        default:
                                            holder.message.setText(chat.getMsg());
                                            break;
                                    }
                                    SimpleDateFormat formatter = new SimpleDateFormat("h:mm");
                                    String dateString = formatter.format(new Date(Long.parseLong(chat.getTimestamp())));
                                    holder.time.setText(dateString);
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

        FirebaseFirestore.getInstance().collection("users").document(modelChatLists.get(position).getId()).addSnapshotListener((value, error) -> {
            holder.name.setText(Objects.requireNonNull(value.get("name")).toString());
            FirebaseFirestore.getInstance().collection("privacy").document(modelChatLists.get(position).getId()).addSnapshotListener((value2, error2) -> {

                if (Objects.requireNonNull(value2).exists()){
                    if (Integer.parseInt(Objects.requireNonNull(value2.get("online")).toString()) == 1 && contactExists(context, PhoneNumberWithoutCountryCode(Objects.requireNonNull(value.get("phone")).toString()))){
                        if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                            Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(holder.dp);
                        }
                    } else if (Integer.parseInt(Objects.requireNonNull(value2.get("online")).toString()) != 2){
                        if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                            Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(holder.dp);
                        }
                    }
                }else {
                    if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                        Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(holder.dp);
                    }
                }

            });

        });

        //Click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("id", modelChatLists.get(position).getId());
            context.startActivity(intent);
            holder.count.setVisibility(View.GONE);
            holder.count.setText("");
        });

        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot ds : snapshot.getChildren()){

                    if (Objects.requireNonNull(ds.child("sender").getValue()).toString().equals(modelChatLists.get(position).getId()) && Objects.requireNonNull(ds.child("receiver").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        ChatModel post = ds.getValue(ChatModel.class);
                        assert post != null;
                        if (Boolean.parseBoolean(Objects.requireNonNull(ds.child("isSeen").getValue()).toString()) == false){
                            i++;
                        }
                    }
                }
                if (i != 0){
                    holder.count.setVisibility(View.VISIBLE);
                    holder.count.setText(""+i);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public static String PhoneNumberWithoutCountryCode(String phoneNumberWithCountryCode){
        Pattern compile = Pattern.compile("\\+(?:998|996|995|994|993|992|977|976|975|974|973|972|971|970|968|967|966|965|964|963|962|961|960|886|880|856|855|853|852|850|692|691|690|689|688|687|686|685|683|682|681|680|679|678|677|676|675|674|673|672|670|599|598|597|595|593|592|591|590|509|508|507|506|505|504|503|502|501|500|423|421|420|389|387|386|385|383|382|381|380|379|378|377|376|375|374|373|372|371|370|359|358|357|356|355|354|353|352|351|350|299|298|297|291|290|269|268|267|266|265|264|263|262|261|260|258|257|256|255|254|253|252|251|250|249|248|246|245|244|243|242|241|240|239|238|237|236|235|234|233|232|231|230|229|228|227|226|225|224|223|222|221|220|218|216|213|212|211|98|95|94|93|92|91|90|86|84|82|81|66|65|64|63|62|61|60|58|57|56|55|54|53|52|51|49|48|47|46|45|44\\D?1624|44\\D?1534|44\\D?1481|44|43|41|40|39|36|34|33|32|31|30|27|20|7|1\\D?939|1\\D?876|1\\D?869|1\\D?868|1\\D?849|1\\D?829|1\\D?809|1\\D?787|1\\D?784|1\\D?767|1\\D?758|1\\D?721|1\\D?684|1\\D?671|1\\D?670|1\\D?664|1\\D?649|1\\D?473|1\\D?441|1\\D?345|1\\D?340|1\\D?284|1\\D?268|1\\D?264|1\\D?246|1\\D?242|1)\\D?");
        return phoneNumberWithCountryCode.replaceAll(compile.pattern(), "");
    }

    public boolean contactExists(Context context, String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        }
        finally {
            if (cur != null){
                cur.close();
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return modelChatLists.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{

        final CircleImageView dp;
        final ImageView online;
        final TextView name;
        final TextView time;
        final TextView message;
        final TextView count;
        final RelativeLayout ad;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.phone);
            count = itemView.findViewById(R.id.count);
            time = itemView.findViewById(R.id.time);
            online = itemView.findViewById(R.id.online);
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
