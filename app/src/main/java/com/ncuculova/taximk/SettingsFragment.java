package com.ncuculova.taximk;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by ncuculova on 5.9.15.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
