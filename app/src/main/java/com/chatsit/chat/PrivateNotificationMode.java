package com.chatsit.chat;

import android.content.Context;
import android.content.SharedPreferences;

public class PrivateNotificationMode {
    final SharedPreferences sharedPreferences;
    public PrivateNotificationMode(Context context){
        sharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);

    }
    public void setPrivateState(Boolean state){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("private", state);
        editor.apply();
    }
    public boolean loadNightModeState(){
        return sharedPreferences.getBoolean("private", true);
    }
}
