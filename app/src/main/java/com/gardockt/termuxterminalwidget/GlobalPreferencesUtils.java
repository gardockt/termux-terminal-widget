package com.gardockt.termuxterminalwidget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class GlobalPreferencesUtils {
    public static final String KEY_DEFAULT_COLOR_FOREGROUND = "default_color_foreground";
    public static final String KEY_DEFAULT_COLOR_BACKGROUND = "default_color_background";

    private GlobalPreferencesUtils() {}

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
