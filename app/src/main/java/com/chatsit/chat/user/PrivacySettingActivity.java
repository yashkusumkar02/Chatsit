
package com.chatsit.chat.user;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ALL")
public class PrivacySettingActivity extends AppCompatActivity {

    int dpDefault;
    int onlineDefault;
    int lastDefault;
    int aboutDefault;
    int whoDefault;
    boolean read;

    NightMode sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.NormalDarkTheme);
        } else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_setting);

        findViewById(R.id.imageView).setOnClickListener(view -> onBackPressed());

        findViewById(R.id.dpChange).setOnClickListener(view -> {
            final CharSequence[] items = { "Everyone", "Contact", "No One"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Who can see your profile photo ?");
            builder.setSingleChoiceItems(items,dpDefault,
                    (dialog, item) -> {
                        dpDefault = item;
                        dialog.dismiss();
                        setToText();
                    });
            builder.show();
        });

        findViewById(R.id.onlineChange).setOnClickListener(view -> {
            final CharSequence[] items = { "Everyone", "Contact", "No One"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Who can see your online status ?");
            builder.setSingleChoiceItems(items,onlineDefault,
                    (dialog, item) -> {
                        onlineDefault = item;
                        dialog.dismiss();
                        setToText();
                    });
            builder.show();
        });

        findViewById(R.id.lastChange).setOnClickListener(view -> {
            final CharSequence[] items = { "Everyone", "Contact", "No One"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Who can see your online status ?");
            builder.setSingleChoiceItems(items,lastDefault,
                    (dialog, item) -> {
                        lastDefault = item;
                        dialog.dismiss();
                        setToText();
                    });
            builder.show();
        });

        findViewById(R.id.aboutChange).setOnClickListener(view -> {
            final CharSequence[] items = { "Everyone", "Contact", "No One"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Who can see your about ?");
            builder.setSingleChoiceItems(items,aboutDefault,
                    (dialog, item) -> {
                        aboutDefault = item;
                        dialog.dismiss();
                        setToText();
                    });
            builder.show();
        });

        findViewById(R.id.whoChange).setOnClickListener(view -> {
            final CharSequence[] items = { "Everyone", "Contact", "No One"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Who can text you ?");
            builder.setSingleChoiceItems(items,whoDefault,
                    (dialog, item) -> {
                        whoDefault = item;
                        dialog.dismiss();
                        setToText();
                    });
            builder.show();
        });

        Switch readRec = findViewById(R.id.read);
        readRec.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                readRec.setChecked(true);
                read = true;
            }else {
                readRec.setChecked(false);
                read = false;
                setToText();
            }
        });

        AsyncTask.execute(() -> {
            FirebaseFirestore.getInstance().collection("privacy").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {

                if (Objects.requireNonNull(value).exists()){
                    dpDefault = Integer.parseInt(Objects.requireNonNull(value.get("dp")).toString());
                    onlineDefault = Integer.parseInt(Objects.requireNonNull(value.get("online")).toString());
                    lastDefault = Integer.parseInt(Objects.requireNonNull(value.get("last")).toString());
                    aboutDefault = Integer.parseInt(Objects.requireNonNull(value.get("about")).toString());
                    whoDefault = Integer.parseInt(Objects.requireNonNull(value.get("who")).toString());
                    read = Boolean.parseBoolean(Objects.requireNonNull(value.get("read")).toString());
                    readRec.setChecked(read);
                    setToText();
                }else {
                     dpDefault = 0;
                     onlineDefault = 0;
                     lastDefault = 0;
                     aboutDefault = 0;
                     whoDefault = 0;
                     read = false;
                    setToText();
                }

            });
        });

        findViewById(R.id.save).setOnClickListener(view -> {
            Map<String,Object> hashMap = new HashMap<>();
            hashMap.put("dp", ""+dpDefault);
            hashMap.put("online", ""+onlineDefault);
            hashMap.put("last", ""+lastDefault);
            hashMap.put("about", ""+aboutDefault);
            hashMap.put("read", ""+read);
            hashMap.put("who", ""+whoDefault);
            FirebaseFirestore.getInstance().collection("privacy").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addSnapshotListener((value, error) -> {
                if (Objects.requireNonNull(value).exists()){
                    FirebaseFirestore.getInstance().collection("privacy").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(hashMap);
                }else {
                    FirebaseFirestore.getInstance().collection("privacy").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(hashMap);
                }
                Snackbar.make(view, "Saved", Snackbar.LENGTH_SHORT).show();
            });
        });



    }

    private void setToText() {

        final CharSequence[] items = { "Everyone", "Contact", "No One"};

        TextView dp = findViewById(R.id.dp);
        TextView online = findViewById(R.id.online);
        TextView last = findViewById(R.id.last);
        TextView about = findViewById(R.id.about);
        TextView who = findViewById(R.id.who);
        Switch readRec = findViewById(R.id.read);

        dp.setText(items[dpDefault]);
        online.setText(items[onlineDefault]);
        last.setText(items[lastDefault]);
        about.setText(items[aboutDefault]);
        who.setText(items[whoDefault]);
        readRec.setChecked(read);

    }


}