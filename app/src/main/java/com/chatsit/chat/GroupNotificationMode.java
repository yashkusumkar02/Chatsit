package com.chatsit.chat;

import android.content.Context;
import android.content.SharedPreferences;

public class GroupNotificationMode {
    final SharedPreferences sharedPreferences;
    public GroupNotificationMode(Context context){
        sharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);

    }
    public void setGroupState(Boolean state){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("group", state);
        editor.apply();
    }
    public boolean loadNightModeState(){
        return sharedPreferences.getBoolean("group", true);
    }
}
