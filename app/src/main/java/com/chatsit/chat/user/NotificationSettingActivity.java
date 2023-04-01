package com.chatsit.chat.user;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CheckBox;

import com.google.android.material.snackbar.Snackbar;
import com.chatsit.chat.GroupNotificationMode;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.PrivateNotificationMode;
import com.chatsit.chat.R;

@SuppressWarnings("ALL")
public class NotificationSettingActivity extends AppCompatActivity {

    GroupNotificationMode groupNotificationMode;
    PrivateNotificationMode privateNotificationMode;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.NormalDarkTheme);
        } else setTheme(R.style.NormalDayTheme);
        groupNotificationMode = new GroupNotificationMode(this);
        privateNotificationMode = new PrivateNotificationMode(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_setting);

        findViewById(R.id.imageView).setOnClickListener(view -> onBackPressed());

        CheckBox group = findViewById(R.id.group);
        CheckBox mPrivate = findViewById(R.id.chat);

        if (!groupNotificationMode.loadNightModeState()){
           group.setChecked(false);
        }

        if (!privateNotificationMode.loadNightModeState()){
            mPrivate.setChecked(false);
        }

        findViewById(R.id.save).setOnClickListener(view -> {
            if (group.isChecked()){
                groupNotificationMode.setGroupState(true);
            }else {
                groupNotificationMode.setGroupState(false);
            }
            if (mPrivate.isChecked()){
                privateNotificationMode.setPrivateState(true);
            }else {
                privateNotificationMode.setPrivateState(false);
            }
            Snackbar.make(view, "Saved", Snackbar.LENGTH_SHORT).show();
        });

    }
}