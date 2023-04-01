package com.chatsit.chat.user;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
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
import com.chatsit.chat.GetTimeAgo;
import com.chatsit.chat.MainActivity;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;
import com.chatsit.chat.Stickers;
import com.chatsit.chat.ThemeMode;
import com.chatsit.chat.adapter.AdapterChat;
import com.chatsit.chat.calling.CallingActivity;
import com.chatsit.chat.calling.RingingActivity;
import com.chatsit.chat.groupVoiceCall.RingingGroupVoiceActivity;
import com.chatsit.chat.model.ChatModel;
import com.chatsit.chat.notification.Data;
import com.chatsit.chat.notification.Sender;
import com.chatsit.chat.notification.Token;
import com.chatsit.chat.party.PartyActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import timber.log.Timber;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

@SuppressWarnings("ALL")
public class ChatActivity extends AppCompatActivity implements GiphyDialogFragment.GifSelectionListener{

    String hisId, myId;
    private RequestQueue requestQueue;
    private boolean notify = false;
    BottomSheetDialog chat_more,theme_more;

    private static final int PERMISSION_REQ_CODE = 1 << 5;
    private final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final String fileName = "recorded.3gp";
    final String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + fileName;
    MediaRecorder mediaRecorder;

    //Declare
    LinearProgressIndicator progressIndicator;

    //Chat
    RecyclerView recyclerView;
    AdapterChat adapterChat;
    List<ChatModel> nChat;
    TextView name;

    String msg = "";

    NightMode sharedPref;

    TextView textView;


    public final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            msg = intent.getStringExtra("text");
            findViewById(R.id.reply).setVisibility(View.VISIBLE);
            textView.setText(msg);
        }
    };

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
        setContentView(R.layout.activity_chat);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-message"));

        check();

        textView = findViewById(R.id.reply_text);

        requestQueue = Volley.newRequestQueue(ChatActivity.this);

        hisId = getIntent().getStringExtra("id");
        myId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //Declare
        findViewById(R.id.back).setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });
        name = findViewById(R.id.name);
        TextView status = findViewById(R.id.status);
        progressIndicator = findViewById(R.id.progressBar);
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

        addTheme();


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
                    hashMap.put("typing", hisId);
                    findViewById(R.id.send).setVisibility(View.VISIBLE);
                    findViewById(R.id.record_button).setVisibility(View.GONE);
                }
                FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(hashMap);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Voice
        findViewById(R.id.call).setOnClickListener(view -> {
            String room = ""+System.currentTimeMillis();
            //noinspection MismatchedQueryAndUpdateOfCollection
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("to", hisId);
            hashMap.put("room", room);
            hashMap.put("call", "voice");
            hashMap.put("type", "calling");
            FirebaseDatabase.getInstance().getReference().child("calling").child(room).setValue(hashMap).addOnCompleteListener(task -> {
                sendVoiceMsg();
                Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
                intent.putExtra("room", room);
                intent.putExtra("to", hisId);
                intent.putExtra("call", "voice");
                startActivity(intent);
            });
        });

        //Voice
        findViewById(R.id.video).setOnClickListener(view -> {
            String room = ""+System.currentTimeMillis();
            //noinspection MismatchedQueryAndUpdateOfCollection
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("to", hisId);
            hashMap.put("room", room);
            hashMap.put("call", "video");
            hashMap.put("type", "calling");
            FirebaseDatabase.getInstance().getReference().child("calling").child(room).setValue(hashMap).addOnCompleteListener(task -> {
                sendVideoMsg();
                Intent intent = new Intent(ChatActivity.this, CallingActivity.class);
                intent.putExtra("room", room);
                intent.putExtra("to", hisId);
                intent.putExtra("call", "video");
                startActivity(intent);
            });
        });

        //Send
        findViewById(R.id.send).setOnClickListener(view -> {

            if (!msg.isEmpty()){
                reply(msg, editText.getText().toString());
            }else {
                HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", FirebaseAuth.getInstance().getCurrentUser().getUid());
            hashMap.put("receiver", hisId);
            hashMap.put("msg", editText.getText().toString());
            hashMap.put("isSeen", false);
            hashMap.put("timestamp", ""+System.currentTimeMillis());
            hashMap.put("type", "text");
            FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
            notify = true;

            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

                if (notify){
                    sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), editText.getText().toString());
                    editText.setText("");
                }
                notify = false;

            });
            }


        });

        findViewById(R.id.delete).setOnClickListener(view -> {
            msg = "";
            findViewById(R.id.reply).setVisibility(View.GONE);
        });

        addAttachment();

        //bottomNav
        findViewById(R.id.add).setOnClickListener(view -> chat_more.show());

        //Chat
        recyclerView = findViewById(R.id.chats);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Sticker
        if (getIntent().hasExtra("uri")){
            sendSticker();
        }


        findViewById(R.id.name).setOnClickListener(view -> {
            Intent intent = new Intent(ChatActivity.this, UserActivity.class);
            intent.putExtra("user", hisId);
            startActivity(intent);
        });
        findViewById(R.id.status).setOnClickListener(view -> {
            Intent intent = new Intent(ChatActivity.this, UserActivity.class);
            intent.putExtra("user", hisId);
            startActivity(intent);
        });

        seenMessage();
        AsyncTask.execute(() -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        if (ds.child("Participants").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()){
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
            Query query = FirebaseDatabase.getInstance().getReference().child("calling").orderByChild("to").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for (DataSnapshot ds : snapshot.getChildren()){
                            if (Objects.requireNonNull(ds.child("type").getValue()).toString().equals("calling")){
                                Intent intent = new Intent(ChatActivity.this, RingingActivity.class);
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
            FirebaseDatabase.getInstance().getReference("users").child(hisId).addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    //Online
                    if (Objects.requireNonNull(snapshot.child("last").getValue()).toString().equals("online")) {
                        status.setText("Online");
                    }else {
                        status.setText((GetTimeAgo.getTimeAgo(Long.parseLong(Objects.requireNonNull(snapshot.child("last").getValue()).toString()))));
                    }

                    MediaPlayer mp = MediaPlayer.create(ChatActivity.this, R.raw.typing);

                    //Typing
                    if (Objects.requireNonNull(snapshot.child("typing").getValue()).toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        findViewById(R.id.typing).setVisibility(View.VISIBLE);
                        mp.start();
                    }else {
                        findViewById(R.id.typing).setVisibility(View.GONE);
                        mp.stop();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            FirebaseFirestore.getInstance().collection("users").document(hisId).addSnapshotListener((value, error) -> {
                name.setText(Objects.requireNonNull(value.get("name")).toString());
                FirebaseFirestore.getInstance().collection("privacy").document(hisId).addSnapshotListener((value2, error2) -> {

                    if (Objects.requireNonNull(value2).exists()){
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("online")).toString()) == 2){
                            status.setVisibility(View.GONE);
                        }
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("last")).toString()) == 2){
                            status.setVisibility(View.GONE);
                        }
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("online")).toString()) == 1 && !contactExists(ChatActivity.this, PhoneNumberWithoutCountryCode(Objects.requireNonNull(value.get("phone")).toString()))){
                            status.setVisibility(View.GONE);
                        }
                        if (Integer.parseInt(Objects.requireNonNull(value2.get("last")).toString()) == 1 && !contactExists(ChatActivity.this, PhoneNumberWithoutCountryCode(Objects.requireNonNull(value.get("phone")).toString()))){
                            status.setVisibility(View.GONE);
                        }
                    }

                });
            });
            chatList();
            readMessage();
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

    private void chatList() {
        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(hisId);
        chatRef1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef1.child("id").setValue(hisId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(hisId)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    chatRef2.child("id").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendVideoMsg() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg", name.getText().toString() + " has video called");
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", ""+System.currentTimeMillis());
        hashMap.put("type", "video_call");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

            if (notify){
                sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " is video calling you");
            }
            notify = false;

        });
    }

    private void sendVoiceMsg() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg", name.getText().toString() + " has voice called");
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", ""+System.currentTimeMillis());
        hashMap.put("type", "voice");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

            if (notify){
                sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " is voice calling you");
            }
            notify = false;

        });
    }

    private void readMessage(){
        nChat = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nChat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatModel chat = snapshot.getValue(ChatModel.class);
                    if (snapshot.hasChild("msg")){
                        if (Objects.requireNonNull(chat).getReceiver().equals(myId) && chat.getSender().equals(hisId) ||
                            chat.getReceiver().equals(hisId) && chat.getSender().equals(myId)){
                        nChat.add(chat);
                    }
                    }
                    adapterChat = new AdapterChat(ChatActivity.this, nChat);
                    recyclerView.setAdapter(adapterChat);
                    adapterChat.notifyDataSetChanged();
                    SwipeController controller = new SwipeController(ChatActivity.this, position -> {
                        if (nChat.get(position).getMsg().equals("reply")){
                            Toast.makeText(ChatActivity.this, "You cant reply on reply", Toast.LENGTH_SHORT).show();
                        }else {
                            msg = nChat.get(position).getMsg();
                            findViewById(R.id.reply).setVisibility(View.VISIBLE);
                            textView.setText(msg);
                        }
                    });
                    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(controller);
                    itemTouchHelper.attachToRecyclerView(recyclerView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void reply(String hisMsg,String reply) {
        String id = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg", id);
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", id);
        hashMap.put("type", "reply");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap).addOnCompleteListener(task -> {
            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("id", id);
            hashMap1.put("reply", reply);
            hashMap1.put("msg", hisMsg);
            FirebaseDatabase.getInstance().getReference().child("Reply").child(id).setValue(hashMap1);
        });
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

            if (notify){
                sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has replied on your message");
            }
            notify = false;

        });

        msg = "";
        findViewById(R.id.reply).setVisibility(View.GONE);

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
                Giphy.INSTANCE.configure(ChatActivity.this, "LpHCYFEd73GGn7A6mh29w2Ey6C2yPcR4", false);

                final GPHSettings settings = new GPHSettings();
                settings.setTheme(GPHTheme.Automatic);
                final GiphyDialogFragment dialog = GiphyDialogFragment.Companion.newInstance(settings);
                dialog.show(getSupportFragmentManager(), "giphy_dialog");
                chat_more.dismiss();
            });

            view.findViewById(R.id.sticker).setOnClickListener(view1 -> {

                Intent intent = new Intent(ChatActivity.this, Stickers.class);
                intent.putExtra("id", hisId);
                startActivity(intent);
                finish();
                chat_more.dismiss();
            });

            view.findViewById(R.id.party).setOnClickListener(view1 -> {
                startActivity(new Intent(ChatActivity.this, PartyActivity.class));
                chat_more.dismiss();
            });

            view.findViewById(R.id.zoom).setOnClickListener(view1 -> {
                startActivity(new Intent(ChatActivity.this, MeetingActivity.class));
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

    private void sendThemeMsg() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg", "Theme changed");
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", ""+System.currentTimeMillis());
        hashMap.put("type", "theme");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 12 && data != null){
            Uri img_uri = Objects.requireNonNull(data).getData();
            sendImage(img_uri);
        }

        if(resultCode == RESULT_OK && requestCode == 11 && data != null){
            Uri video_uri = Objects.requireNonNull(data).getData();
            sendVideo(video_uri);
        }

        if(resultCode == RESULT_OK && requestCode == 13 && data != null){
            Uri audio_uri = Objects.requireNonNull(data).getData();
            sendAudio(audio_uri);
        }

        if(resultCode == RESULT_OK && requestCode == 14 && data != null){
            Uri send_doc = Objects.requireNonNull(data).getData();
            sendDoc(send_doc);
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

    private void sendLocation(double currentLatitude, double currentLongitude) {

        String id = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg", id);
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", id);
        hashMap.put("type", "location");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap).addOnCompleteListener(task -> {
            HashMap<String, Object> hashMap2 = new HashMap<>();
            hashMap2.put("lat", String.valueOf(currentLatitude));
            hashMap2.put("long", String.valueOf(currentLongitude));
            hashMap2.put("id", id);
            FirebaseDatabase.getInstance().getReference().child("Location").child(id).setValue(hashMap2);
        });
        notify = true;

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
            if (notify){
                sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent you location");
            }
            notify = false;

        });
    }

    private void restartApp() {

        Intent intent = new Intent(ChatActivity.this, ChatActivity.class);
        intent.putExtra("id", hisId);
        startActivity(intent);

    }


    private void sendRec() {
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        Uri audio_uri = Uri.fromFile(new File(file));
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_audio/" + ""+System.currentTimeMillis());
        storageReference.putFile(audio_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            //noinspection StatementWithEmptyBody
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg", downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "audio");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                notify = true;

                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent audio recording");
                    }
                    notify = false;

                });
            }
        });

    }

    private void sendDoc(Uri send_doc) {
        progressIndicator.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_doc/" + ""+System.currentTimeMillis());
        storageReference.putFile(send_doc).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            //noinspection StatementWithEmptyBody
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg",  downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "doc");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                notify = true;
                progressIndicator.setVisibility(View.GONE);

                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent document");
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
            //noinspection StatementWithEmptyBody
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg",  downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "audio");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                notify = true;
                progressIndicator.setVisibility(View.GONE);

                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent  video");
                    }
                    notify = false;

                });
            }
        });
    }

    private void sendVideo(Uri video_uri) {
        progressIndicator.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_video/" + ""+System.currentTimeMillis());
        storageReference.putFile(video_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            //noinspection StatementWithEmptyBody
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg",  downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "video");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                notify = true;
                progressIndicator.setVisibility(View.GONE);

                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent  audio");
                    }
                    notify = false;

                });
            }
        });
    }

    private void sendImage(Uri img_uri) {
        progressIndicator.setVisibility(View.VISIBLE);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("chat_photo/" + ""+System.currentTimeMillis());
        storageReference.putFile(img_uri).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            //noinspection StatementWithEmptyBody
            while (!uriTask.isSuccessful()) ;
            Uri downloadUri = uriTask.getResult();
            if (uriTask.isSuccessful()){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                hashMap.put("receiver", hisId);
                hashMap.put("msg",  downloadUri.toString());
                hashMap.put("isSeen", false);
                hashMap.put("timestamp", ""+System.currentTimeMillis());
                hashMap.put("type", "image");
                FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
                
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                notify = true;

                progressIndicator.setVisibility(View.GONE);

                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {

                    if (notify){
                        sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent  image");
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
        hashMap.put("typing", "no");
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
    public void onBackPressed() {
        super.onBackPressed();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("last", ""+System.currentTimeMillis());
        hashMap.put("typing", "no");
        FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).updateChildren(hashMap);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void sendNotification(final String hisId, final String name,final String message){
        DatabaseReference allToken = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allToken.orderByKey().equalTo(hisId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), name + " : " + message, "New Message", hisId, "chat", R.drawable.logo);
                    assert token != null;
                    Sender sender = new Sender(data, token.getToken());
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, response -> Timber.d("onResponse%s", response.toString()), error -> Timber.d("onResponse%s", error.toString())){
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAeaMBuL0:APA91bHjQTIPnp_T7SlaSpcwttqMs5GiN2zdES6BAGgygpbgDnhBK1_u5KxizIRTz1iZl-m56nLD8IUelSTmiK62ddj8yzzvAlNP5LL4xyDyspRZDduxfuQ2G_Qzx2AUa81_A2M_P5srChatsiT");
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

    private void check() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        //noinspection StatementWithEmptyBody
        if (granted) {

        } else {
            requestPermissions();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
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

    @SuppressLint("ObsoleteSdkInt")
    private void setOther() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            hashMap.put("receiver", hisId);
            hashMap.put("msg",  Objects.requireNonNull(mediaResponse.getData().getImages().getOriginal()).getGifUrl());
            hashMap.put("isSeen", false);
            hashMap.put("timestamp", ""+System.currentTimeMillis());
            hashMap.put("type", "gif");
            FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
            notify = true;
            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
                if (notify){
                    sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent GIF");
                }
                notify = false;
            });


            return null;
        });


    }

    private void sendSticker() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg",  getIntent().getStringExtra("uri"));
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", ""+System.currentTimeMillis());
        hashMap.put("type", "sticker");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap);
        notify = true;
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
            if (notify){
                sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent sticker");
            }
            notify = false;
        });
    }

    private void sendContact(String contactName, String contactNumber) {
        String id = ""+System.currentTimeMillis();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        hashMap.put("receiver", hisId);
        hashMap.put("msg",  id);
        hashMap.put("isSeen", false);
        hashMap.put("timestamp", ""+System.currentTimeMillis());
        hashMap.put("type", "contact");
        FirebaseDatabase.getInstance().getReference().child("Chats").push().setValue(hashMap).addOnCompleteListener(task -> {
            HashMap<String, Object> hashMap2 = new HashMap<>();
            hashMap2.put("name", contactName);
            hashMap2.put("number", contactNumber);
            hashMap2.put("id", id);
            FirebaseDatabase.getInstance().getReference().child("Contact").child(id).setValue(hashMap2);
        });
        notify = true;
        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
            if (notify){
                sendNotification(hisId, Objects.requireNonNull(value.get("name")).toString(), " has sent contact details of "+contactName);
            }
            notify = false;
        });
    }

    private void seenMessage(){
        FirebaseDatabase.getInstance().getReference().child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (Objects.requireNonNull(snapshot.child("receiver").getValue()).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()) &&
                            Objects.requireNonNull(snapshot.child("sender").getValue()).toString().equals(hisId)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
