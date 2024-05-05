package com.serhat.aieditor.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Prefs {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private static Prefs instance;

    private Prefs(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public static Prefs getInstance(Context context) {
        if (instance == null) {
            instance = new Prefs(context);
        }
        return instance;
    }

    public void setInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }


    public void setLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }


    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }


    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setFloat(String key, float value) {
        editor.putFloat(key, value).apply();
    }

    public void removeKey(String key) {
        editor.remove(key);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean def) {
        return sharedPreferences.getBoolean(key, def);
    }

    public int getInt(String key, int def) {
        return sharedPreferences.getInt(key, def);
    }

    public float getFloat(String key, float def) {
        return sharedPreferences.getFloat(key, def);
    }


    public long getLong(String key, long def) {
        return sharedPreferences.getLong(key, def);
    }


    public String getString(String key, String def) {
        return sharedPreferences.getString(key, def);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
