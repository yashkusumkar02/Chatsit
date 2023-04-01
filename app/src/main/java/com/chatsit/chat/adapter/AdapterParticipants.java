package com.chatsit.chat.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.chatsit.chat.R;
import com.chatsit.chat.model.UserModel;
import com.chatsit.chat.notification.Data;
import com.chatsit.chat.notification.Sender;
import com.chatsit.chat.notification.Token;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


@SuppressWarnings("ALL")
public class AdapterParticipants extends RecyclerView.Adapter<AdapterParticipants.HolderParticipantsAdd>{

    private final Context context;
    private final List<UserModel> userList;
    private final String groupId;
    private final String myGroupRole;
    private RequestQueue requestQueue;
    private boolean notify = false;
    String mUsername;

    public AdapterParticipants(Context context, List<UserModel> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderParticipantsAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.create_user_list, parent, false);
        return new HolderParticipantsAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderParticipantsAdd holder, int position) {

        requestQueue = Volley.newRequestQueue(context);


        FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").orderByChild("id").equalTo(userList.get(position).getId()).
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

        UserModel modelUser = userList.get(position);
        String mName = modelUser.getName();
         mUsername = modelUser.getPhone();
        String uid = modelUser.getId();

        holder.name.setText(mName);


        FirebaseFirestore.getInstance().collection("privacy").document(modelUser.getId()).addSnapshotListener((value2, error2) -> {

            if (Objects.requireNonNull(value2).exists()){
                if (Integer.parseInt(Objects.requireNonNull(value2.get("online")).toString()) == 1 && contactExists(context, PhoneNumberWithoutCountryCode(modelUser.getPhone()))){
                    if (!modelUser.getPhoto().isEmpty()){
                        Picasso.get().load(modelUser.getPhoto()).into(holder.circleImageView);
                    }
                } else if (Integer.parseInt(Objects.requireNonNull(value2.get("online")).toString()) != 2){
                    if (!modelUser.getPhoto().isEmpty()){
                        Picasso.get().load(modelUser.getPhoto()).into(holder.circleImageView);
                    }

                }
            }else {
                if (!modelUser.getPhoto().isEmpty()){
                    Picasso.get().load(modelUser.getPhoto()).into(holder.circleImageView);
                }
            }

        });


        checkAlreadyExists(modelUser, holder,mUsername);
        holder.itemView.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).child("Participants").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String hisPrevRole = ""+snapshot.child("role").getValue();
                                String[] options;
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Choose Option");
                                if (myGroupRole.equals("creator")){
                                    if (hisPrevRole.equals("admin")){
                                        options = new String[]{"Remove Admin", "Remove User"};
                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 0) {
                                                removeAdmin(modelUser);

                                            } else {
                                                removeParticipants(modelUser);
                                            }
                                        }).show();
                                    }
                                    else if (hisPrevRole.equals("participant"))
                                    {
                                        options = new String[]{"Make Admin", "Remove User"};
                                        builder.setItems(options, (dialog, which) -> {
                                            if (which == 0) {
                                                makeAdmin(modelUser);

                                            } else {
                                                removeParticipants(modelUser);
                                            }
                                        }).show();

                                    }
                                }
                                else if (myGroupRole.equals("admin")){
                                    switch (hisPrevRole) {
                                        case "creator":
                                            Snackbar.make(holder.itemView, "Creator of the group", Snackbar.LENGTH_LONG).show();
                                            break;
                                        case "admin":
                                            options = new String[]{"Remove Admin", "Remove User"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    removeAdmin(modelUser);

                                                } else {
                                                    removeParticipants(modelUser);
                                                }
                                            }).show();
                                            break;
                                        case "participant":
                                            options = new String[]{"Make Admin", "Remove User"};
                                            builder.setItems(options, (dialog, which) -> {
                                                if (which == 0) {
                                                    makeAdmin(modelUser);

                                                } else {
                                                    removeParticipants(modelUser);
                                                }
                                            }).show();
                                            break;
                                    }
                                }
                            }
                            else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Add Participant")
                                        .setMessage("Add this user in this group?")
                                        .setPositiveButton("Add", (dialog, which) -> addParticipants(modelUser)).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        });
        holder.username.setText(mUsername);

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


    private void addParticipants(UserModel modelUser) {
        String timestamp = ""+System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", modelUser.getId());
        hashMap.put("role", "participant");
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("group", groupId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).setValue(hashMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User added", Toast.LENGTH_SHORT).show());
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {

            if (notify){
                sendNotification(modelUser.getId(), Objects.requireNonNull(value.get("name")).toString(), " you are added to group");
            }
            notify = false;

        });

        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap2.put("msg", mUsername+" added to group");
        hashMap2.put("type", "add");
        hashMap2.put("timestamp", stamp);
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap2);

    }

    private void makeAdmin(UserModel modelUser) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Admin made", Toast.LENGTH_SHORT).show());
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {

            if (notify){
                sendNotification(modelUser.getId(), Objects.requireNonNull(value.get("name")).toString(), " you are admin of group");
            }
            notify = false;

        });

        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap2.put("msg", mUsername+" is admin now");
        hashMap2.put("type", "add");
        hashMap2.put("timestamp", stamp);
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap2);

    }

    private void removeParticipants(UserModel modelUser) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User removed from the group", Toast.LENGTH_SHORT).show());
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {

            if (notify){
                sendNotification(modelUser.getId(), Objects.requireNonNull(value.get("name")).toString(), " you are removed from the group");
            }
            notify = false;

        });

        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap2.put("msg", mUsername+" has been removed");
        hashMap2.put("type", "add");
        hashMap2.put("timestamp", stamp);
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap2);

    }

    private void removeAdmin(UserModel modelUser) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "participant");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId()).updateChildren(hashMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Admin removed", Toast.LENGTH_SHORT).show());
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {

            if (notify){
                sendNotification(modelUser.getId(), Objects.requireNonNull(value.get("name")).toString(), " you are removed from the group admin");
            }
            notify = false;

        });

        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
        hashMap2.put("msg", mUsername+" has been removed as admin");
        hashMap2.put("type", "add");
        hashMap2.put("timestamp", stamp);
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap2);

    }

    private void checkAlreadyExists(UserModel modelUser, HolderParticipantsAdd holder, String mUsername) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(modelUser.getId())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String hisRole = ""+snapshot.child("role").getValue();
                            holder.username.setText(mUsername + " - " +hisRole);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendNotification(final String hisId, final String name,final String message){
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " : " + message, "New Message", hisId, "group",R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        //noinspection deprecation
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key="+context.getResources().getString(R.string.server_key));
                                return headers;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class HolderParticipantsAdd extends RecyclerView.ViewHolder{

        final CircleImageView circleImageView;
        final TextView name;
        final TextView username;
        final RelativeLayout ad;

        public HolderParticipantsAdd(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.dp);
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
