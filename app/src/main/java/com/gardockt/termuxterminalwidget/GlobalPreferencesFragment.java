package com.gardockt.termuxterminalwidget;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class GlobalPreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.global_preferences, rootKey);
    }
}
