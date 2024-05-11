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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        int colorForeground = sharedPreferences.getInt(
                KEY_DEFAULT_COLOR_FOREGROUND,
                context.getColor(R.color.widget_default_color_foreground)
        );
        int colorBackground = sharedPreferences.getInt(
                KEY_DEFAULT_COLOR_BACKGROUND,
                context.getColor(R.color.widget_default_color_background)
        );

        GlobalPreferences preferences = new GlobalPreferences(colorForeground, colorBackground);
        preferencesSubject.onNext(preferences);
        return preferences;
    }

    public static void save(@NonNull Context context, @NonNull GlobalPreferences preferences) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPreferences.edit()
                .putInt(KEY_DEFAULT_COLOR_FOREGROUND, preferences.getColorForeground())
                .putInt(KEY_DEFAULT_COLOR_BACKGROUND, preferences.getColorBackground())
                .apply();

        preferencesSubject.onNext(preferences);
    }
}
