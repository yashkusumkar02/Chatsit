package com.chatsit.chat.party;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterInviteGroups;
import com.chatsit.chat.adapter.AdapterInviteUser;
import com.chatsit.chat.model.ModelGroups;
import com.chatsit.chat.model.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class InviteMoreActivity extends AppCompatActivity {

    //User
    RecyclerView recyclerView;
    List<UserModel> createModels;
    AdapterInviteUser adapterCreateChat;
    List<String> numberList;

    AdapterInviteGroups adapterInviteGroups;
    List<ModelGroups> modelGroups;
    RecyclerView groups_chat;

    private static String room;
    public static String getRoom() {
        return room;
    }
    public InviteMoreActivity(){

    }
    NightMode sharedPref;
    
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        room = getIntent().getStringExtra("room");

        findViewById(R.id.next).setVisibility(View.GONE);

        findViewById(R.id.imageView).setOnClickListener(v -> FirebaseDatabase.getInstance().getReference().child("Party").child(room).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")){
                    Intent intent = new Intent(getApplicationContext(), StartYouTubeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", room);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(getApplicationContext(), StartPartyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", room);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }));

        //User
        recyclerView = findViewById(R.id.users);
        numberList = new ArrayList<>();
        createModels = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkPermission();

        groups_chat = findViewById(R.id.group);
        groups_chat.setLayoutManager(new LinearLayoutManager(this));
        modelGroups = new ArrayList<>();

        AsyncTask.execute(this::getChats);

    }

    public static String PhoneNumberWithoutCountryCode(String phoneNumberWithCountryCode){
        Pattern compile = Pattern.compile("\\+(?:998|996|995|994|993|992|977|976|975|974|973|972|971|970|968|967|966|965|964|963|962|961|960|886|880|856|855|853|852|850|692|691|690|689|688|687|686|685|683|682|681|680|679|678|677|676|675|674|673|672|670|599|598|597|595|593|592|591|590|509|508|507|506|505|504|503|502|501|500|423|421|420|389|387|386|385|383|382|381|380|379|378|377|376|375|374|373|372|371|370|359|358|357|356|355|354|353|352|351|350|299|298|297|291|290|269|268|267|266|265|264|263|262|261|260|258|257|256|255|254|253|252|251|250|249|248|246|245|244|243|242|241|240|239|238|237|236|235|234|233|232|231|230|229|228|227|226|225|224|223|222|221|220|218|216|213|212|211|98|95|94|93|92|91|90|86|84|82|81|66|65|64|63|62|61|60|58|57|56|55|54|53|52|51|49|48|47|46|45|44\\D?1624|44\\D?1534|44\\D?1481|44|43|41|40|39|36|34|33|32|31|30|27|20|7|1\\D?939|1\\D?876|1\\D?869|1\\D?868|1\\D?849|1\\D?829|1\\D?809|1\\D?787|1\\D?784|1\\D?767|1\\D?758|1\\D?721|1\\D?684|1\\D?671|1\\D?670|1\\D?664|1\\D?649|1\\D?473|1\\D?441|1\\D?345|1\\D?340|1\\D?284|1\\D?268|1\\D?264|1\\D?246|1\\D?242|1)\\D?");
        return phoneNumberWithCountryCode.replaceAll(compile.pattern(), "");
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(InviteMoreActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(InviteMoreActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }else {
            getAllUsers();
        }
    }

    private void getChats() {
        FirebaseDatabase.getInstance().getReference("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelGroups.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    if (ds.child("Participants").hasChild(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        ModelGroups modelChatListGroups = ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(ModelGroups.class);
                        modelGroups.add(modelChatListGroups);
                    }
                    adapterInviteGroups = new AdapterInviteGroups(InviteMoreActivity.this, modelGroups);
                    groups_chat.setAdapter(adapterInviteGroups);
                    if (adapterInviteGroups.getItemCount() == 0){
                        groups_chat.setVisibility(View.GONE);
                        findViewById(R.id.group_title).setVisibility(View.GONE);
                    }else {
                        groups_chat.setVisibility(View.VISIBLE);
                        findViewById(R.id.group_title).setVisibility(View.VISIBLE);
                    }
                }

                if (!dataSnapshot.exists()){
                    groups_chat.setVisibility(View.GONE);
                    findViewById(R.id.group_title).setVisibility(View.GONE);
                }

                checkPermission();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllUsers() {

        ContentResolver contentResolver = getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        @SuppressLint("Recycle") Cursor cursor = contentResolver.query(uri, null, null, null,null);
        if (cursor.getCount() > 0){
            while (cursor.moveToNext()){
                @SuppressLint("Range") String contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                numberList.add(contactNumber);
            }
            getAllUsersFromDatabase();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllUsersFromDatabase() {

        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            createModels.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                for (String no : numberList){
                    if (PhoneNumberWithoutCountryCode(Objects.requireNonNull(ds.get("phone")).toString()).equals(PhoneNumberWithoutCountryCode(no))  &&
                            !Objects.requireNonNull(ds.get("id")).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        createModels.add(ds.toObject(UserModel.class));
                        break;
                    }
                }
                adapterCreateChat = new AdapterInviteUser(this, createModels);
                recyclerView.setAdapter(adapterCreateChat);
                adapterCreateChat.notifyDataSetChanged();
                if (adapterCreateChat.getItemCount() == 0){
                    findViewById(R.id.users_title).setVisibility(View.GONE);
                    findViewById(R.id.users).setVisibility(View.GONE);
                }else {
                    findViewById(R.id.users_title).setVisibility(View.VISIBLE);
                    findViewById(R.id.users).setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getAllUsers();
        }else {
            Snackbar.make(findViewById(R.id.main), "Permission is needed", Snackbar.LENGTH_LONG).show();
            checkPermission();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseDatabase.getInstance().getReference().child("Party").child(room).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (Objects.requireNonNull(snapshot.child("type").getValue()).toString().equals("upload_youtube")){
                    Intent intent = new Intent(getApplicationContext(), StartYouTubeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", room);
                    startActivity(intent);
                    finish();
                }else {
                    Intent intent = new Intent(getApplicationContext(), StartPartyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("room", room);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}