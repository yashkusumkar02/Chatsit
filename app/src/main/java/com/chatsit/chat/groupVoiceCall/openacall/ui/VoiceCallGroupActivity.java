package com.chatsit.chat.groupVoiceCall.openacall.ui;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.R;
import com.chatsit.chat.adapter.AdapterGroupActiveUsers;
import com.chatsit.chat.group.GroupChatActivity;
import com.chatsit.chat.groupVoiceCall.openacall.model.AGEventHandler;
import com.chatsit.chat.groupVoiceCall.openacall.model.ConstantApp;
import com.chatsit.chat.model.UserModel;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

@SuppressWarnings("ALL")
public class VoiceCallGroupActivity extends BaseActivity implements AGEventHandler {


    private final static Logger log = LoggerFactory.getLogger(VoiceCallGroupActivity.class);

    private volatile boolean mAudioMuted = false;

    private volatile int mAudioRouting = -1; // Default

    Chronometer chronometer;
    boolean isRunning;
    String channelName;
    String groupId;
    List<String> activeList;
    RecyclerView userList;
    AdapterGroupActiveUsers adapterUsers;
    List<UserModel> modelUsers;
    String fromId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_voice_call);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void initUIandEvent() {

        groupId = getIntent().getStringExtra("group");

        chronometer = findViewById(R.id.chronometer);

        userList = findViewById(R.id.members);

        if (!isRunning){
            chronometer.start();
            isRunning = true;
        }


        event().addEventHandler(this);

        Intent i = getIntent();

        channelName = i.getStringExtra(ConstantApp.ACTION_KEY_CHANNEL_NAME);


        worker().joinChannel(channelName, config().mUid);

        optional();

        FirebaseFirestore.getInstance().collection("groups").document(groupId).addSnapshotListener((value, error) -> {
            TextView name = findViewById(R.id.name);
            name.setText(value.get("name").toString());
            CircleImageView dp = findViewById(R.id.dp);
            if (!value.get("photo").toString().isEmpty())  Picasso.get().load(value.get("photo").toString()).into(dp);
        });


        //Query
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(channelName);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("type").getValue().toString().equals("end") || !snapshot.child("ans").exists() ){
                    ref.removeEventListener(this);
                    Intent intent = new Intent(VoiceCallGroupActivity.this, GroupChatActivity.class);
                    intent.putExtra("group", groupId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                fromId = snapshot.child("from").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Users
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL, false);
        userList.setLayoutManager(linearLayoutManager);
        userList.setHasFixedSize(true);
        activeList = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(channelName).child("ans").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    activeList.clear();
                    activeList.add(fromId);
                    for (DataSnapshot ds : snapshot.getChildren()){
                        activeList.add(ds.getKey());
                    }
                    loadUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void loadUsers() {
        modelUsers = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("users").addSnapshotListener((value, error) -> {
            modelUsers.clear();
            for (DocumentSnapshot ds : Objects.requireNonNull(value)){
                for (String no : activeList){
                    if (ds.get("id").toString().equals(no)   &&
                            !Objects.requireNonNull(ds.get("id")).toString().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())){
                        if (!ds.get("id").toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            modelUsers.add(ds.toObject(UserModel.class));
                        }
                    }
                }
                adapterUsers = new AdapterGroupActiveUsers(VoiceCallGroupActivity.this, modelUsers);
                userList.setAdapter(adapterUsers);
                adapterUsers.notifyDataSetChanged();
            }
        });
    }

    private Handler mMainHandler;

    private static final int UPDATE_UI_MESSAGE = 0x1024;

    EditText mMessageList;

    StringBuffer mMessageCache = new StringBuffer();

    private void notifyMessageChanged(String msg) {
        if (mMessageCache.length() > 10000) { // drop messages
            mMessageCache = new StringBuffer(mMessageCache.substring(10000 - 40));
        }

        mMessageCache.append(System.currentTimeMillis()).append(": ").append(msg).append("\n"); // append timestamp for messages

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mMainHandler == null) {
                    mMainHandler = new Handler(getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);

                            if (isFinishing()) {
                                return;
                            }

                            if (msg.what == UPDATE_UI_MESSAGE) {
                                String content = (String) (msg.obj);
                                mMessageList.setText(content);
                                mMessageList.setSelection(content.length());
                            }

                        }
                    };

                    //noinspection RedundantCast
                    mMessageList = (EditText) findViewById(R.id.msg_list);
                }

                mMainHandler.removeMessages(UPDATE_UI_MESSAGE);
                Message envelop = new Message();
                envelop.what = UPDATE_UI_MESSAGE;
                envelop.obj = mMessageCache.toString();
                mMainHandler.sendMessageDelayed(envelop, 1000l);
            }
        });
    }

    private void optional() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void optionalDestroy() {
    }

    public void onSwitchSpeakerClicked(View view) {
        log.info("onSwitchSpeakerClicked " + view + " " + mAudioMuted + " " + mAudioRouting);

        RtcEngine rtcEngine = rtcEngine();


        rtcEngine.setEnableSpeakerphone(mAudioRouting != 3);
    }

    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();

        doLeaveChannel();
        event().removeEventHandler(this);
    }


    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
    }

    public void onEndCallClicked(View view) {
        log.info("onEndCallClicked " + view);
        quitCall();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        log.info("onBackPressed");

        quitCall();
    }

    private void quitCall() {

        if (isRunning){
            chronometer.stop();
            isRunning = false;
        }

        //Query
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(channelName);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("from").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    Toast.makeText(VoiceCallGroupActivity.this, "Ended", Toast.LENGTH_SHORT).show();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("type", "end");
                    FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(channelName)
                            .updateChildren(hashMap);
                    ref.removeEventListener(this);
                }else {
                    ref.removeEventListener(this);
                    Toast.makeText(VoiceCallGroupActivity.this, "Ended", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(channelName).child("end").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                    FirebaseDatabase.getInstance().getReference("Groups").child(groupId).child("Voice").child(channelName).child("ans").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onVoiceMuteClicked(View view) {
        log.info("onVoiceMuteClicked " + view + " audio_status: " + mAudioMuted);

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);

        ImageView iv = (ImageView) view;

        if (mAudioMuted) {
            iv.setImageResource(R.drawable.ic_mic_off);
        } else {
            iv.setImageResource(R.drawable.ic_mic);
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        String msg = "onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL) + " " + elapsed;
        log.debug(msg);

        notifyMessageChanged(msg);

        runOnUiThread(() -> {
            if (isFinishing()) {
                return;
            }

            rtcEngine().muteLocalAudioStream(mAudioMuted);
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        String msg = "onUserOffline " + (uid & 0xFFFFFFFFL) + " " + reason;
        log.debug(msg);

        notifyMessageChanged(msg);

    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(() -> {
            if (isFinishing()) {
                return;
            }

            doHandleExtraCallback(type, data);
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED: {
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                notifyMessageChanged("mute: " + (peerUid & 0xFFFFFFFFL) + " " + muted);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_QUALITY: {
                peerUid = (Integer) data[0];
                int quality = (int) data[1];
                short delay = (short) data[2];
                short lost = (short) data[3];

                notifyMessageChanged("quality: " + (peerUid & 0xFFFFFFFFL) + " " + quality + " " + delay + " " + lost);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS: {
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                StringBuilder volumeCache = new StringBuilder();
                for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
                    peerUid = each.uid;
                    int peerVolume = each.volume;

                    if (peerUid == 0) {
                        continue;
                    }

                    volumeCache.append("volume: ").append(peerUid & 0xFFFFFFFFL).append(" ").append(peerVolume).append("\n");
                }

                if (volumeCache.length() > 0) {
                    String volumeMsg = volumeCache.substring(0, volumeCache.length() - 1);
                    notifyMessageChanged(volumeMsg);

                    if ((System.currentTimeMillis() / 1000) % 10 == 0) {
                        log.debug(volumeMsg);
                    }
                }
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR: {
                int subType = (int) data[0];

                if (subType == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
                    showLongToast("No network connection, please check the network settings");
                }

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];

                notifyMessageChanged(error + " " + description);

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED: {
                notifyHeadsetPlugged((int) data[0]);

                break;
            }
        }
    }

    public void notifyHeadsetPlugged(final int routing) {
        log.info("notifyHeadsetPlugged " + routing);

        mAudioRouting = routing;

        @SuppressWarnings("RedundantCast") ImageView iv = (ImageView) findViewById(R.id.switch_speaker_id);
        if (mAudioRouting == 3) { // Speakerphone
            iv.setImageResource(R.drawable.ic_speaker);
        } else {
            iv.setImageResource(R.drawable.ic_low);
        }
    }
}
