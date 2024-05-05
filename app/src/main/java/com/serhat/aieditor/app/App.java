package com.serhat.aieditor.app;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;


import com.serhat.aieditor.preference.Prefs;

public class App extends Application {

    public static App SELF_INSTANCE;

    @Override
    public void onCreate() {
        super.onCreate();
        SELF_INSTANCE = this;
        Prefs prefs = Prefs.getInstance(getApplicationContext());

        switch (prefs.getString("themeChoice", "4")) {
            case "0":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "2":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_TIME);
                break;
            case "3":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case "4":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }


    public static App getInstance() {
        return SELF_INSTANCE;
    }

    public boolean isStoragePermissionGranted() {
        return ActivityCompat.checkSelfPermission(App.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
