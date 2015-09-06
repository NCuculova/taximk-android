package com.example.ncuculova.taxinadica;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by ncuculova on 5.9.15.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
