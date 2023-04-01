package com.chatsit.chat;

import android.content.Context;
import android.content.SharedPreferences;

public class NightMode {
    final SharedPreferences sharedPreferences;
    public NightMode(Context context){
        sharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);

    }
    public void setNightModeState(Boolean state){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }
    public boolean loadNightModeState(){
        return sharedPreferences.getBoolean("NightMode", false);
    }
}
