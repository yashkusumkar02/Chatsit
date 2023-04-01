package com.chatsit.chat.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.GetTimeAgo;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.calling.RingingActivity;
import com.chatsit.chat.groupVoiceCall.RingingGroupVoiceActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
public class UserActivity extends AppCompatActivity {

    NightMode sharedPref;

    String hisId;
    BottomSheetDialog chat_more;
    boolean isBlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_user_profile);

        hisId = getIntent().getStringExtra("user");

        //Back
        findViewById(R.id.imageView).setOnClickListener(view -> onBackPressed());

        //Text
        TextView name = findViewById(R.id.name);
        TextView email = findViewById(R.id.email);
        TextView bio = findViewById(R.id.bio);
        TextView phone = findViewById(R.id.phone);
        ImageView dp = findViewById(R.id.dp);
        TextView status = findViewById(R.id.status);

        AsyncTask.execute(() -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        if (ds.child("Participants").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).exists()){
                            for (DataSnapshot dataSnapshot1 : ds.child("Voice").getChildren()){
                                if (Objects.requireNonNull(dataSnapshot1.child("type").getValue()).toString().equals("calling")){
                                    if (!Objects.requireNonNull(dataSnapshot1.child("from").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                        if (!dataSnapshot1.child("end").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            if (!dataSnapshot1.child("ans").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                                Intent intent = new Intent(getApplicationContext(), RingingGroupVoiceActivity.class);
                                                intent.putExtra("room", Objects.requireNonNull(dataSnapshot1.child("room").getValue()).toString());
                                                intent.putExtra("group", Objects.requireNonNull(ds.child("groupId").getValue()).toString());
                                                startActivity(intent);
                                                finish();
                                                ref.removeEventListener(this);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("to").equalTo(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (Objects.requireNonNull(ds.child("type").getValue()).toString().equals("calling")){
                                Intent intent = new Intent(getApplicationContext(), RingingActivity.class);
                                intent.putExtra("room", Objects.requireNonNull(ds.child("room").getValue()).toString());
                                intent.putExtra("from", Objects.requireNonNull(ds.child("from").getValue()).toString());
                                intent.putExtra("call", Objects.requireNonNull(ds.child("call").getValue()).toString());
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            FirebaseDatabase.getInstance().getReference("users").child(hisId).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (Objects.requireNonNull(snapshot.child("last").getValue()).toString().equals("online")) {
                        status.setText("Online");
                    }else {
                        status.setText((GetTimeAgo.getTimeAgo(Long.parseLong(Objects.requireNonNull(snapshot.child("last").getValue()).toString()))));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseFirestore.getInstance().collection("users").document(hisId).addSnapshotListener((value, error) -> {

                FirebaseFirestore.getInstance().collection("privacy").document(hisId).addSnapshotListener((value2, error2) -> {

                    if (Objects.requireNonNull(value2).exists()){
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("about")).toString()) == 2){
                            bio.setVisibility(View.GONE);
                        }
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("dp")).toString()) == 2){
                            Picasso.get().load(R.drawable.avatar).into(dp);
                        }
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("about")).toString()) == 1 && !contactExists(UserActivity.this, PhoneNumberWithoutCountryCode(Objects.requireNonNull(value.get("phone")).toString()))){
                            bio.setVisibility(View.GONE);
                        }
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("dp")).toString()) == 1 && contactExists(UserActivity.this, PhoneNumberWithoutCountryCode(Objects.requireNonNull(value.get("phone")).toString()))){
                            if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                                Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(dp);
                            }
                        }
                    }else {
                        if (!Objects.requireNonNull(value.get("photo")).toString().isEmpty()){
                            Picasso.get().load(Objects.requireNonNull(value.get("photo")).toString()).into(dp);
                        }
                    }

                });

                name.setText(Objects.requireNonNull(value.get("name")).toString());
                email.setText(Objects.requireNonNull(value.get("email")).toString());
                bio.setText(Objects.requireNonNull(value.get("bio")).toString());
                phone.setText(Objects.requireNonNull(value.get("phone")).toString());

            });

            DatabaseReference refm = FirebaseDatabase.getInstance().getReference("Users");
            refm.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").orderByChild("id").equalTo(hisId).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()){
                                if (ds.exists()){
                                    isBlocked = true;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        });

        addAttachment();

        findViewById(R.id.createPost).setOnClickListener(view -> chat_more.show());

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

    private void BlockUser() {
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("id", hisId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").child(hisId).setValue(hashMap);
        Snackbar.make(findViewById(R.id.createPost), "Blocked",Snackbar.LENGTH_LONG).show();
        isBlocked = true;

    }

    private void unBlockUser() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).child("BlockedUsers").orderByChild("id").equalTo(hisId).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if (Objects.requireNonNull(ds.child("id").getValue()).toString().equals(hisId)){
                                ds.getRef().removeValue();
                                Snackbar.make(findViewById(R.id.createPost), "UnBlocked",Snackbar.LENGTH_LONG).show();
                                isBlocked = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(findViewById(R.id.createPost), error.getMessage(),Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void addAttachment() {
        if (chat_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.user_bottom, null);

            TextView textBlock = view.findViewById(R.id.blockText);

            if (isBlocked){
                textBlock.setText("Unblock");
            }else {
                textBlock.setText("Block");
            }

            view.findViewById(R.id.block).setOnClickListener(view1 -> {
                if (isBlocked){
                    unBlockUser();
                }else {
                    BlockUser();
                }
                chat_more.dismiss();
            });

            view.findViewById(R.id.chat).setOnClickListener(view1 -> {
                Intent intent = new Intent(UserActivity.this, ChatActivity.class);
                intent.putExtra("id", hisId);
                startActivity(intent);
                chat_more.dismiss();
            });

            view.findViewById(R.id.report).setOnClickListener(view1 -> {
                FirebaseDatabase.getInstance().getReference().child("Report").child(hisId).setValue(true);
                Snackbar.make(findViewById(R.id.createPost), "Reported",Snackbar.LENGTH_LONG).show();
                chat_more.dismiss();
            });

            chat_more = new BottomSheetDialog(this);
            chat_more.setContentView(view);
        }
    }

}