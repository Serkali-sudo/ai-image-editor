package com.serhat.aieditor.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.serhat.aieditor.R;
import com.serhat.aieditor.preference.Prefs;

public class SettingsFragment extends PreferenceFragmentCompat {

    private ListPreference mThemeList;

    private Preference github_key;

    private FragmentActivity activity;

    private Context context;

    private Prefs prefs;


    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
            assert key != null;
            if (key.equals("themeChoice")) {
                setSummaryTheme(sharedPreferences.getString("themeChoice", "4"));
            }
        }
    };

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        context = getContext();
        activity = getActivity();
        prefs = Prefs.getInstance(activity.getApplicationContext());
        setPreferencesFromResource(R.xml.preference, rootKey);
        mThemeList = (ListPreference) findPreference("themeChoice");

        github_key = findPreference("github_key");

        github_key.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://github.com/Serkali-sudo/AI_Image_Editor"));
                    startActivity(intent);
                } catch (Exception ignored) {
                }
                return false;
            }
        });

        setSummaryTheme(prefs.getString("themeChoice", "4"));
        prefs.getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);
    }

    private void setSummaryTheme(String theme) {
        switch (theme) {
            case "0":
                mThemeList.setSummary("Light");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "1":
                mThemeList.setSummary("Dark");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "2":
                mThemeList.setSummary("Auto (By Time)");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_TIME);
                break;
            case "3":
                mThemeList.setSummary("Auto (By Battery)");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                break;
            case "4":
                mThemeList.setSummary("Auto (Follow System)");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

}
