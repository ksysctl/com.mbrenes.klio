package com.mbrenes.klio;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.annotation.TargetApi;

public class Settings extends PreferenceActivity {
    private static int preferences = R.layout.activity_preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            AddResourceOld();
        } else {
            AddResourceNew();
        }
    }

    @SuppressWarnings("deprecation")
    protected void AddResourceOld() {
        addPreferencesFromResource(preferences);
    }

    @TargetApi(11)
    protected void AddResourceNew() {
        getFragmentManager().beginTransaction().replace(
            android.R.id.content, new SettingsFragment()
        ).commit();
    }

    @TargetApi(11)
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(Settings.preferences);
        }
    }
}
