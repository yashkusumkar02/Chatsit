package com.chatsit.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Objects;

public class TranslationActivity extends AppCompatActivity {

    NightMode sharedPref;
    SharedMode sharedMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        sharedMode = new SharedMode(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.NormalDarkTheme);
        }else setTheme(R.style.NormalDayTheme);
        if (!sharedMode.loadNightModeState().isEmpty()){
            setApplicationLocale(sharedMode.loadNightModeState());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);


        findViewById(R.id.english).setOnClickListener(view -> {

            sharedMode.setNightModeState("en");
            restartApp();

        });

        findViewById(R.id.hindi).setOnClickListener(view -> {

            sharedMode.setNightModeState("hi");
            restartApp();

        });

        findViewById(R.id.arabic).setOnClickListener(view -> {

            sharedMode.setNightModeState("ar");
            restartApp();

        });



        findViewById(R.id.german).setOnClickListener(view -> {

            sharedMode.setNightModeState("de");
            restartApp();

        });

        findViewById(R.id.spain).setOnClickListener(view -> {

            sharedMode.setNightModeState("es");
            restartApp();

        });

        findViewById(R.id.france).setOnClickListener(view -> {

            sharedMode.setNightModeState("fr");
            restartApp();

        });

        findViewById(R.id.italy).setOnClickListener(view -> {

            sharedMode.setNightModeState("it");
            restartApp();

        });

        findViewById(R.id.japan).setOnClickListener(view -> {

            sharedMode.setNightModeState("ja");
            restartApp();

        });

        findViewById(R.id.china).setOnClickListener(view -> {

            sharedMode.setNightModeState("zh");
            restartApp();

        });


        findViewById(R.id.russia).setOnClickListener(view -> {

            sharedMode.setNightModeState("ru");
            restartApp();

        });

        findViewById(R.id.portugal).setOnClickListener(view -> {

            sharedMode.setNightModeState("pt");
            restartApp();

        });

        findViewById(R.id.urdu).setOnClickListener(view -> {

            sharedMode.setNightModeState("ur");
            restartApp();

        });

        findViewById(R.id.turkish).setOnClickListener(view -> {

            sharedMode.setNightModeState("tr");
            restartApp();

        });


        findViewById(R.id.telgu).setOnClickListener(view -> {

            sharedMode.setNightModeState("te");
            restartApp();

        });

        findViewById(R.id.tamil).setOnClickListener(view -> {

            sharedMode.setNightModeState("ta");
            restartApp();

        });

        findViewById(R.id.marathi).setOnClickListener(view -> {

            sharedMode.setNightModeState("mr");
            restartApp();

        });

        findViewById(R.id.amharic).setOnClickListener(view -> {

            sharedMode.setNightModeState("am");
            restartApp();

        });


        findViewById(R.id.oromo).setOnClickListener(view -> {

            sharedMode.setNightModeState("om");
            restartApp();

        });

        findViewById(R.id.igbo).setOnClickListener(view -> {

            sharedMode.setNightModeState("ig");
            restartApp();

        });

        findViewById(R.id.yoruba).setOnClickListener(view -> {

            sharedMode.setNightModeState("yo");
            restartApp();

        });

        findViewById(R.id.zulu).setOnClickListener(view -> {

            sharedMode.setNightModeState("zu");
            restartApp();

        });



        findViewById(R.id.dutch).setOnClickListener(view -> {

            sharedMode.setNightModeState("nl");
            restartApp();

        });

        findViewById(R.id.bengali).setOnClickListener(view -> {

            sharedMode.setNightModeState("bn");
            restartApp();

        });

        findViewById(R.id.hausa).setOnClickListener(view -> {

            sharedMode.setNightModeState("ha");
            restartApp();

        });

        findViewById(R.id.swahili).setOnClickListener(view -> {

            sharedMode.setNightModeState("sw");
            restartApp();

        });


        findViewById(R.id.vietnamese).setOnClickListener(view -> {

            sharedMode.setNightModeState("vi");
            restartApp();

        });


    }

    @SuppressLint({"ObsoleteSdkInt", "AppBundleLocaleChanges"})
    private void setApplicationLocale(String locale) {
        Resources resources = getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(new Locale(locale.toLowerCase()));
        } else {
            config.locale = new Locale(locale.toLowerCase());
        }
        resources.updateConfiguration(config, dm);
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        Objects.requireNonNull(i).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}