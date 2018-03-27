package com.shilpasweth.android_bus_tracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by shilpa on 3/25/2018.
 */

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
