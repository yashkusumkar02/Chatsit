package com.chatsit.chat;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedMode {
    final SharedPreferences sharedPreferences;
    public SharedMode(Context context){
        sharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);

    }
    public void setNightModeState(String state){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", state);
        editor.apply();
    }
    public String loadNightModeState(){
        return sharedPreferences.getString("name", "en");
    }
}
