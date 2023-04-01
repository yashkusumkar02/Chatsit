package com.chatsit.chat.party;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class StartPartyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String id = getIntent().getStringExtra("room");

        Intent intent = new Intent(getApplicationContext(), WatchPartyActivity.class);
        intent.putExtra("room", id);
        startActivity(intent);
        finish();

    }
}