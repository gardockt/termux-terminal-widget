package com.gardockt.termuxterminalwidget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class GlobalPreferencesUtils {
    private static final String KEY_DEFAULT_COLOR_FOREGROUND = "default_color_foreground";
    private static final String KEY_DEFAULT_COLOR_BACKGROUND = "default_color_background";

    private static final BehaviorSubject<GlobalPreferences> preferencesSubject = BehaviorSubject.create();

    private GlobalPreferencesUtils() {}

    @NonNull
    public static GlobalPreferences get(@NonNull Context context) {
        GlobalPreferences value = preferencesSubject.getValue();
        if (value == null) {
            value = load(context);
        }
        return value.clone();
    }

    // Returning as Observable and not BehaviorSubject, so that the value cannot be changed from outside.
    public static Observable<GlobalPreferences> getObservable(@NonNull Context context) {
        if (preferencesSubject.getValue() == null) {
            load(context);
        }
        return preferencesSubject
                .map(GlobalPreferences::clone);
    }

    @NonNull
    private static GlobalPreferences load(@NonNull Context context) {
        GlobalPreferences preferences = new GlobalPreferences();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        Integer colorForeground = null;
        Integer colorBackground = null;

        if (sharedPreferences.contains(KEY_DEFAULT_COLOR_FOREGROUND)) {
            colorForeground = sharedPreferences.getInt(KEY_DEFAULT_COLOR_FOREGROUND, 0);
        }
        if (sharedPreferences.contains(KEY_DEFAULT_COLOR_BACKGROUND)) {
            colorBackground = sharedPreferences.getInt(KEY_DEFAULT_COLOR_BACKGROUND, 0);
        }
        if (colorForeground != null && colorBackground != null) {
            ColorScheme colorScheme = new ColorScheme(colorForeground, colorBackground);
            preferences.setColorScheme(colorScheme);
        }

        preferencesSubject.onNext(preferences);
        return preferences;
    }

    public static void save(@NonNull Context context, @NonNull GlobalPreferences preferences) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPreferences.edit()
                .putInt(KEY_DEFAULT_COLOR_FOREGROUND, preferences.getColorScheme().getColorForeground())
                .putInt(KEY_DEFAULT_COLOR_BACKGROUND, preferences.getColorScheme().getColorBackground())
                .apply();

        preferencesSubject.onNext(preferences);
    }
}
