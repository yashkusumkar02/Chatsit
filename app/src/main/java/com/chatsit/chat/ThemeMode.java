package com.chatsit.chat;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeMode {
    final SharedPreferences sharedPreferences;
    public ThemeMode(Context context){
        sharedPreferences = context.getSharedPreferences("filename", Context.MODE_PRIVATE);

    }
    public void setNightModeState(String state){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("theme", state);
        editor.apply();
    }
    public String loadNightModeState(){
        return sharedPreferences.getString("theme", "day");
    }
}
