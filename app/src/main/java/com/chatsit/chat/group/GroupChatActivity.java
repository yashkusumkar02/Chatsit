package com.chatsit.chat.group;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.capybaralabs.swipetoreply.SwipeController;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.essam.simpleplacepicker.MapActivity;
import com.essam.simpleplacepicker.utils.SimplePlacePicker;
import com.giphy.sdk.core.GPHCore;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.chatsit.chat.GroupStickers;
import com.chatsit.chat.MainActivity;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.ThemeMode;
import com.chatsit.chat.adapter.AdapterGroupActiveUsers;
import com.chatsit.chat.adapter.AdapterGroupChat;
import com.chatsit.chat.calling.RingingActivity;
import com.chatsit.chat.groupVideoCall.CallingGroupVideoActivity;
import com.chatsit.chat.groupVoiceCall.CallingGroupVoiceActivity;
import com.chatsit.chat.groupVoiceCall.RingingGroupVoiceActivity;
import com.chatsit.chat.model.ModelGroupChat;
import com.chatsit.chat.model.UserModel;
import com.chatsit.chat.notification.Data;
import com.chatsit.chat.notification.Sender;
import com.chatsit.chat.notification.Token;
import com.chatsit.chat.party.PartyActivity;
import com.chatsit.chat.user.MeetingActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

@SuppressWarnings("ALL")
public class GroupChatActivity extends AppCompatActivity implements GiphyDialogFragment.GifSelectionListener{

    //String
    String mName;
    TextView name;
    private static String groupId;
    public static String getGroupId() {
        return groupId;
    }
    public GroupChatActivity(){

    }
    BottomSheetDialog chat_more,theme_more;

    public static final String fileName = "recorded.3gp";
    final String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName;
    MediaRecorder mediaRecorder;

    //Permission
    private static final int PERMISSION_REQ_CODE = 1 << 3;
    private final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    //ID
    LinearLayout main;
    private ArrayList<ModelGroupChat> groupChats;
    private AdapterGroupChat adapterGroupChat;
    RecyclerView recyclerView;
    private ArrayList<UserModel> userArrayList;
    private AdapterGroupActiveUsers adapterParticipants;
    RecyclerView onlineList;

    private RequestQueue requestQueue;
    private boolean notify = false;

    NightMode sharedPref;
    String msg = "";

    TextView textView;
    LinearProgressIndicator progressIndicator;

    public final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            msg = intent.getStringExtra("text");
            findViewById(R.id.reply).setVisibility(View.VISIBLE);
            textView.setText(msg);
        }
    };

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    ThemeMode themeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        themeMode = new ThemeMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.DarkChatTheme);
        }else {
            if (themeMode.loadNightModeState().equals("day")) {
                setDay();
            } else if (themeMode.loadNightModeState().equals("red")) {
                setTheme(R.style.RedChatTheme);
                setOther();
            } else if (themeMode.loadNightModeState().equals("purple")) {
                setTheme(R.style.PurpleChatTheme);
                setOther();
            } else if (themeMode.loadNightModeState().equals("green")) {
                setTheme(R.style.GreenChatTheme);
                setOther();
            } else if (themeMode.loadNightModeState().equals("maroon")) {
                setTheme(R.style.MarronChatTheme);
                setOther();
            } else if (themeMode.loadNightModeState().equals("navy")) {
                setTheme(R.style.NavyChatTheme);
                setOther();
            } else if (themeMode.loadNightModeState().equals("orange")) {
                setTheme(R.style.OrangeChatTheme);
                setOther();
            } else if (themeMode.loadNightModeState().equals("yellow")) {
                setTheme(R.style.YellowChatTheme);
                setOther();
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_cha);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));

        requestQueue = Volley.newRequestQueue(GroupChatActivity.this);

        textView = findViewById(R.id.reply_text);

        //GetID
        groupId = getIntent().getStringExtra("group");

        //Back
        findViewById(R.id.back).setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));

        addTheme();

        //Id
        main = findViewById(R.id.main);
        name = findViewById(R.id.name);
        TextView members = findViewById(R.id.status);
        recyclerView = findViewById(R.id.chats);
        recyclerView.setHasFixedSize(true);
        progressIndicator = findViewById(R.id.progressBar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        onlineList = findViewById(R.id.online);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false);
        onlineList.setLayoutManager(linearLayoutManager2);
        onlineList.setHasFixedSize(true);
        userArrayList = new ArrayList<>();
        check();

        RecordView recordView = findViewById(R.id.record_view);
        RecordButton recordButton = findViewById(R.id.record_button);
        recordButton.setRecordView(recordView);

        //RecorderCustom
        recordView.setSlideToCancelTextColor(Color.parseColor("#1C2A4D"));

        recordView.setOnRecordListener(new OnRecordListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onStart() {
                findViewById(R.id.record_view).setVisibility(View.VISIBLE);
                findViewById(R.id.other).setVisibility(View.GONE);
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                mediaRecorder.setOutputFile(file);
                startRecording();
            }

            @Override
            public void onCancel() {
                new Handler().postDelayed(() -> {
                    findViewById(R.id.record_view).setVisibility(View.GONE);
                    findViewById(R.id.other).setVisibility(View.VISIBLE);
                },1050);
            }

            @Override
            public void onFinish(long recordTime) {
                stopRecording();
                findViewById(R.id.record_view).setVisibility(View.GONE);
                findViewById(R.id.other).setVisibility(View.VISIBLE);
            }

            @Override
            public void onLessThanSecond() {
                Snackbar.make(findViewById(R.id.main), "Recording must be greater than one Second", Snackbar.LENGTH_SHORT).show();
                findViewById(R.id.record_view).setVisibility(View.GONE);
                findViewById(R.id.other).setVisibility(View.VISIBLE);
            }
        });

        //Sticker
        if (getIntent().hasExtra("uri")){
            sendSticker();
        }


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

            FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Participants").addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    members.setText("Members "+snapshot.getChildrenCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> mName = Objects.requireNonNull(value.get("name")).toString());
            FirebaseFirestore.getInstance().collection("groups").document(groupId).addSnapshotListener((value, error) -> name.setText(Objects.requireNonNull(value.get("name")).toString()));
            loadGroupMessage();

            loadMembers();
        });

        //EditText
        EditText editText = findViewById(R.id.editText);

        //Typing
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HashMap<String, Object> hashMap = new HashMap<>();
                if (count == 0){
                    hashMap.put("typing", "no");
                    findViewById(R.id.send).setVisibility(View.GONE);
                    findViewById(R.id.record_button).setVisibility(View.VISIBLE);
                }else {
                    hashMap.put("typing", groupId);
                    findViewById(R.id.send).setVisibility(View.VISIBLE);
                    findViewById(R.id.record_button).setVisibility(View.GONE);
                }
                FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(hashMap);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Profile
        findViewById(R.id.name).setOnClickListener(view -> {
            Intent intent = new Intent(GroupChatActivity.this, GroupProfile.class);
            intent.putExtra("group", groupId);
            startActivity(intent);
        });
        findViewById(R.id.status).setOnClickListener(view -> {
            Intent intent = new Intent(GroupChatActivity.this, GroupProfile.class);
            intent.putExtra("group", groupId);
            startActivity(intent);
        });

        //Send
        findViewById(R.id.send).setOnClickListener(v -> {

            if (!msg.isEmpty()){
                reply(msg, editText.getText().toString());
            }else{
                String stamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("msg", editText.getText().toString());
                hashMap.put("type", "text");
                hashMap.put("timestamp", stamp);
                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                        .setValue(hashMap);
                notify = true;
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                    UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                    if (notify){
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), editText.getText().toString());
                                    editText.setText("");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    notify = false;

                });
            }

        });

        //VideoCall
        findViewById(R.id.video).setOnClickListener(v -> {
            String room = ""+System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("from", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            hashMap.put("to", groupId);
            hashMap.put("room", room);
            hashMap.put("type", "calling");
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Video").child(room)
                    .setValue(hashMap).addOnCompleteListener(task -> {
                        String stamp = ""+System.currentTimeMillis();
                        HashMap<String, Object> hashMap1 = new HashMap<>();
                        hashMap1.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap1.put("msg", mName + " has video called");
                        hashMap1.put("type", "video_call");
                        hashMap1.put("timestamp", stamp);
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                                .setValue(hashMap1);
                        Intent intent = new Intent(GroupChatActivity.this, CallingGroupVideoActivity.class);
                        intent.putExtra("room", room);
                        intent.putExtra("group", groupId);
                        startActivity(intent);
                    });
        });

        //VoiceCall
        findViewById(R.id.call).setOnClickListener(v -> {
            String room = ""+System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("from", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            hashMap.put("to", groupId);
            hashMap.put("room", room);
            hashMap.put("type", "calling");
            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(room)
                    .setValue(hashMap).addOnCompleteListener(task -> {
                        String stamp = ""+System.currentTimeMillis();
                        HashMap<String, Object> hashMap1 = new HashMap<>();
                        hashMap1.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        hashMap1.put("msg", mName + " has voice called");
                        hashMap1.put("type", "voice_call");
                        hashMap1.put("timestamp", stamp);
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                                .setValue(hashMap1);
                        Intent intent = new Intent(GroupChatActivity.this, CallingGroupVoiceActivity.class);
                        intent.putExtra("room", room);
                        intent.putExtra("group", groupId);
                        startActivity(intent);
                    });
        });

        //Bottom
        addAttachment();
        findViewById(R.id.add).setOnClickListener(view -> chat_more.show());
    }

    private void reply(String hisMsg,String reply) {
        String id = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("msg", id);
        hashMap.put("type", "reply");
        hashMap.put("timestamp", id);
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(id)
                .setValue(hashMap).addOnCompleteListener(task -> {
            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("id", id);
            hashMap1.put("reply", reply);
            hashMap1.put("msg", hisMsg);
            FirebaseDatabase.getInstance().getReference().child("Reply").child(id).setValue(hashMap1);
        });

        notify = true;
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
            UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
            if (notify){
                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            sendNotification(ds.getKey(),  Objects.requireNonNull(user).getName(), " has replied on your message");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            notify = false;

        });

        msg = "";
        findViewById(R.id.reply).setVisibility(View.GONE);

    }

    private void addAttachment() {
        if (chat_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.bottom_chat, null);

            view.findViewById(R.id.video).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                //noinspection deprecation
                startActivityForResult(intent, 11);
                chat_more.dismiss();
            });

            view.findViewById(R.id.image).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                //noinspection deprecation
                startActivityForResult(intent, 12);
                chat_more.dismiss();
            });

            view.findViewById(R.id.contact).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                //noinspection deprecation
                startActivityForResult(intent, 15);
                chat_more.dismiss();
            });

            view.findViewById(R.id.mic).setOnClickListener(view1 -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                //noinspection deprecation
                startActivityForResult(intent, 13);
                chat_more.dismiss();
            });

            view.findViewById(R.id.document).setOnClickListener(view1 -> {
                Intent intent;
                if (android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
                    intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                    intent.putExtra("CONTENT_TYPE", "*/*");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    //noinspection deprecation
                    startActivityForResult(intent, 14);
                    chat_more.dismiss();
                } else {

                    String[] mimeTypes =
                            {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "text/plain",
                                    "application/pdf",
                                    "application/zip", "application/vnd.android.package-archive"};

                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    //noinspection deprecation
                    startActivityForResult(intent, 14);
                    chat_more.dismiss();
                }
            });

            view.findViewById(R.id.location).setOnClickListener(view1 -> {

                Intent intent = new Intent(this, MapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(SimplePlacePicker.API_KEY, "AIzaSyCoezJQ7_58c0bLHXF5wBCjA8-5W0BzJ30");
                intent.putExtras(bundle);
                //noinspection deprecation
                startActivityForResult(intent, SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE);

                chat_more.dismiss();
            });

            view.findViewById(R.id.gif).setOnClickListener(view1 -> {
                //GIF
                Giphy.INSTANCE.configure(GroupChatActivity.this, "LpHCYFEd73GGn7A6mh29w2Ey6C2yPcR4", false);

                final GPHSettings settings = new GPHSettings();
                settings.setTheme(GPHTheme.Automatic);
                final GiphyDialogFragment dialog = GiphyDialogFragment.Companion.newInstance(settings);
                dialog.show(getSupportFragmentManager(), "giphy_dialog");
                chat_more.dismiss();
            });

            view.findViewById(R.id.sticker).setOnClickListener(view1 -> {

                Intent intent = new Intent(GroupChatActivity.this, GroupStickers.class);
                intent.putExtra("id", groupId);
                startActivity(intent);
                finish();
                chat_more.dismiss();

            });

            view.findViewById(R.id.party).setOnClickListener(view1 -> {
                startActivity(new Intent(GroupChatActivity.this, PartyActivity.class));
                chat_more.dismiss();
            });

            view.findViewById(R.id.zoom).setOnClickListener(view1 -> {
                startActivity(new Intent(GroupChatActivity.this, MeetingActivity.class));
                chat_more.dismiss();
            });

            view.findViewById(R.id.theme).setOnClickListener(view1 -> {
                theme_more.show();
                chat_more.dismiss();
            });

            chat_more = new BottomSheetDialog(this);
            chat_more.setContentView(view);
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void setOther() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }


    private void loadGroupMessage() {
        groupChats = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(getGroupId()).child("Message")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupChats.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelGroupChat modelGroupChat = ds.getValue(ModelGroupChat.class);
                            groupChats.add(modelGroupChat);
                        }
                        adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this, groupChats);
                        recyclerView.setAdapter(adapterGroupChat);
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                        adapterGroupChat.notifyDataSetChanged();
                        SwipeController controller = new SwipeController(GroupChatActivity.this, position -> {
                            if (groupChats.get(position).getMsg().equals("reply")){
                                Toast.makeText(GroupChatActivity.this, "You cant reply on reply", Toast.LENGTH_SHORT).show();
                            }else {
                                msg = groupChats.get(position).getMsg();
                                findViewById(R.id.reply).setVisibility(View.VISIBLE);
                                textView.setText(msg);
                            }
                        });
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
                        itemTouchHelper.attachToRecyclerView(recyclerView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void restartApp() {

        Intent intent = new Intent(GroupChatActivity.this, GroupChatActivity.class);
        intent.putExtra("group", groupId);
        startActivity(intent);

    }

    private void sendThemeMsg() {
        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("msg", "Theme changed");
        hashMap.put("type", "theme");
        hashMap.put("timestamp", stamp);
        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap);
    }

    private void addTheme() {
        if (theme_more == null){
            @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.theme_more, null);

            view.findViewById(R.id.camera).setOnClickListener(view1 -> {
                themeMode.setNightModeState("day");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.image).setOnClickListener(view1 -> {
                themeMode.setNightModeState("dark");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.video).setOnClickListener(view1 -> {
                themeMode.setNightModeState("red");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.audio).setOnClickListener(view1 -> {
                themeMode.setNightModeState("purple");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.background).setOnClickListener(view1 -> {
                themeMode.setNightModeState("green");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.gif).setOnClickListener(view1 -> {
                themeMode.setNightModeState("maroon");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.feeling).setOnClickListener(view1 -> {
                themeMode.setNightModeState("navy");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.meeting).setOnClickListener(view1 -> {
                themeMode.setNightModeState("orange");
                restartApp();
                sendThemeMsg();
            });

            view.findViewById(R.id.reels).setOnClickListener(view1 -> {
                themeMode.setNightModeState("yellow");
                restartApp();
                sendThemeMsg();
            });

            theme_more = new BottomSheetDialog(this);
            theme_more.setContentView(view);
        }
    }

    private void startRecording() {
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();

        } catch(RuntimeException stopException) {
            // handle cleanup here
        }
        sendRec();
    }

    private void sendRec() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        Toast.makeText(this, "Please wait, Sending...", Toast.LENGTH_SHORT).show();
        Uri audio_uri = Uri.fromFile(new File(file));

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_audio/" + ""+System.currentTimeMillis());
        storageReference.putFile(audio_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){

                String stamp = ""+System.currentTimeMillis();

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("msg",downloadUri.toString());
                hashMap.put("type", "audio");
                hashMap.put("timestamp", stamp);

                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                        .setValue(hashMap);

                findViewById(R.id.progressBar).setVisibility(View.GONE);
                notify = true;
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                    UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                    if (notify){
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent audio recording");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    notify = false;

                });
            }
        });

    }



    @Override
    protected void onPause() {
        super.onPause();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("last", ""+System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("last", "online");
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(hashMap);
    }


    @Override
    protected void onStart() {
        super.onStart();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("last", "online");
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(hashMap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("last", ""+System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(hashMap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                granted = (result == PERMISSION_GRANTED);
                if (!granted) break;
            }
            //noinspection StatementWithEmptyBody
            if (granted) {
            } else {
                Snackbar.make(findViewById(R.id.main), "Permission is required", Snackbar.LENGTH_LONG).show();
                check();
            }
        }
    }


    private void check() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (granted) {

        } else {
            requestPermissions();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 12 && data != null){
            Uri dp_uri = Objects.requireNonNull(data).getData();
            sendImage(dp_uri);
        }
        if(resultCode == RESULT_OK && requestCode == 11 && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();
            sendVideo(video_uri);
        }
        if (resultCode == RESULT_OK && requestCode == 13 && data != null){
            Uri audio_uri = Objects.requireNonNull(data).getData();
            sendAudio(audio_uri);
        }
        if (resultCode == RESULT_OK && requestCode == 14 && data != null){
            Uri doc_uri = Objects.requireNonNull(data).getData();
            sendDoc(doc_uri);
        }

        if (resultCode == RESULT_OK && requestCode == SimplePlacePicker.SELECT_LOCATION_REQUEST_CODE && data != null) {

            double lon = data.getDoubleExtra(SimplePlacePicker.LOCATION_LNG_EXTRA,0);
            double lat = data.getDoubleExtra(SimplePlacePicker.LOCATION_LAT_EXTRA,0);
            sendLocation(lat, lon);

        }

        if (resultCode == RESULT_OK && requestCode == 15 && data != null){
            Cursor cursor1, cursor2;
            Uri uri = data.getData();
            cursor1 = getContentResolver().query(uri, null, null, null,null);
            if (cursor1.moveToFirst()){
                @SuppressLint("Range") String contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String contactName = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                @SuppressLint("Range") String idResult = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                int idResultHold = Integer.parseInt(idResult);
                if (idResultHold == 1){
                    cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "+contactId, null, null);
                    //noinspection LoopStatementThatDoesntLoop
                    while (cursor2.moveToNext()){
                        @SuppressLint("Range") String contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        sendContact(contactName,contactNumber);
                        break;
                    }
                    cursor2.close();
                }
                cursor1.close();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendLocation(double lat, double lon) {
        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("msg", stamp);
        hashMap.put("type", "location");
        hashMap.put("timestamp", stamp);

        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap);

        //Location
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("lat", String.valueOf(lat));
        hashMap2.put("long", String.valueOf(lon));
        hashMap2.put("id", stamp);
        FirebaseDatabase.getInstance().getReference().child("Location").child(stamp).setValue(hashMap2);
        notify = true;
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
            UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
            if (notify){
                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent location");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            notify = false;

        });
    }

    private void sendContact(String contactName, String contactNumber) {

        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("msg", stamp);
        hashMap.put("type", "contact");
        hashMap.put("timestamp", stamp);

        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap).addOnCompleteListener(task -> {
            HashMap<String, Object> hashMap2 = new HashMap<>();
            hashMap2.put("name", contactName);
            hashMap2.put("number", contactNumber);
            hashMap2.put("id", stamp);
            FirebaseDatabase.getInstance().getReference().child("Contact").child(stamp).setValue(hashMap2);
        });
    }

    private void sendDoc(Uri doc_uri) {
        progressIndicator.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_doc/" + ""+System.currentTimeMillis());
        storageReference.putFile(doc_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                String stamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("msg", downloadUri.toString());
                hashMap.put("type", "doc");
                hashMap.put("timestamp", stamp);

                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                        .setValue(hashMap);

                progressIndicator.setVisibility(View.GONE);
                notify = true;
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                    UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                    if (notify){
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent document");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    notify = false;

                });

            }
        });
    }

    private void sendAudio(Uri audio_uri) {
        progressIndicator.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_audio/" + ""+System.currentTimeMillis());
        storageReference.putFile(audio_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){

                String stamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("msg", downloadUri.toString());
                hashMap.put("type", "audio");
                hashMap.put("timestamp", stamp);

                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                        .setValue(hashMap);

                progressIndicator.setVisibility(View.GONE);
                notify = true;
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                    UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                    if (notify){
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent audio");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    notify = false;

                });

            }
        });
    }

    @Override
    public void didSearchTerm(@NotNull String s) {

    }

    @Override
    public void onDismissed(@NotNull GPHContentType gphContentType) {

    }

    @Override
    public void onGifSelected(@NotNull Media media, @org.jetbrains.annotations.Nullable String s, @NotNull GPHContentType gphContentType) {

        GPHCore.INSTANCE.gifById(media.getId(), (mediaResponse, throwable) -> {

            String stamp = ""+System.currentTimeMillis();
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            hashMap.put("msg", Objects.requireNonNull(media.getImages().getOriginal()).getGifUrl());
            hashMap.put("type", "gif");
            hashMap.put("timestamp", stamp);

            FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                    .setValue(hashMap);

            notify = true;
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                if (notify){
                    FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent gif");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                notify = false;

            });

            return null;
        });


    }

    private void sendVideo(Uri videoUri){
        progressIndicator.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_video/" + ""+System.currentTimeMillis());
        storageReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){

                String stamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("msg", downloadUri.toString());
                hashMap.put("type", "video");
                hashMap.put("timestamp", stamp);

                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                        .setValue(hashMap);
                progressIndicator.setVisibility(View.GONE);
                notify = true;
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                    UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                    if (notify){
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent gif");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    notify = false;

                });

            }
        });
    }


    private void sendImage(Uri dp_uri) {
        progressIndicator.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(dp_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){

                String stamp = ""+System.currentTimeMillis();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("msg", downloadUri.toString());
                hashMap.put("type", "image");
                hashMap.put("timestamp", stamp);

                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                        .setValue(hashMap);

                progressIndicator.setVisibility(View.GONE);

                notify = true;
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                    UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
                    if (notify){
                        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()){
                                    sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent image");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    notify = false;

                });

            }
        });

    }

    private void sendSticker() {
        String stamp = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("msg", getIntent().getStringExtra("uri"));
        hashMap.put("type", "sticker");
        hashMap.put("timestamp", stamp);

        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Message").child(stamp)
                .setValue(hashMap);

        notify = true;
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
            UserModel user = Objects.requireNonNull(value).toObject(UserModel.class);
            if (notify){
                FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            sendNotification(ds.getKey(), Objects.requireNonNull(user).getName(), " has sent sticker");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            notify = false;

        });
    }

    private void loadMembers() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("last", "online");
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(hashMap);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(ds.child("id").getValue()).toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (Objects.requireNonNull(snapshot.child("last").getValue()).toString().equals("online")){
                                if (!Objects.requireNonNull(ds.child("id").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(ds.child("id").getValue()).toString()).addSnapshotListener((value, error) -> {
                                        userArrayList.add(Objects.requireNonNull(value).toObject(UserModel.class));
                                    });
                                }
                            }
                            adapterParticipants = new AdapterGroupActiveUsers(GroupChatActivity.this, userArrayList);
                            onlineList.setAdapter(adapterParticipants);
                            adapterParticipants.notifyDataSetChanged();
                            if (adapterParticipants.getItemCount() != 0){
                                onlineList.setVisibility(View.VISIBLE);
                            }else {
                                onlineList.setVisibility(View.GONE);
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
    }


    private void sendNotification(final String hisId, final String name,final String message){
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " : " + message, "New Message", hisId,"group", R.drawable.logo);
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
                                headers.put("Authorization", "key="+getResources().getString(R.string.server_key));
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
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @SuppressLint("ObsoleteSdkInt")
    private void setDay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            //noinspection deprecation
            Drawable background = getResources().getDrawable(R.drawable.gradient_bg);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //noinspection deprecation
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent));
            window.setBackgroundDrawable(background);
        }
    }


}