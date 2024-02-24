package com.gardockt.termuxterminalwidget.mainwidget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.gardockt.termuxterminalwidget.exceptions.InvalidConfigurationException;

public class MainWidgetPreferencesManager {

    private static final String PREFS_NAME = "MainWidget";

    private static final String KEY_COMMAND = "command";
    private static final String KEY_COLOR_FOREGROUND = "color_foreground";
    private static final String KEY_COLOR_BACKGROUND = "color_background";

    @NonNull
    public static MainWidgetPreferences load(@NonNull Context context, int widgetId) throws InvalidConfigurationException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        MainWidgetPreferences preferences;

        String command = sharedPreferences.getString(generateKey(widgetId, KEY_COMMAND), null);
        if (command == null) {
            throw new InvalidConfigurationException("Command is null");
        }

        preferences = new MainWidgetPreferences(command);

        String colorFgKey = generateKey(widgetId, KEY_COLOR_FOREGROUND);
        if (sharedPreferences.contains(colorFgKey)) {
            preferences.setColorForeground(sharedPreferences.getInt(colorFgKey, 0));
        }

        String colorBgKey = generateKey(widgetId, KEY_COLOR_BACKGROUND);
        if (sharedPreferences.contains(colorBgKey)) {
            preferences.setColorBackground(sharedPreferences.getInt(colorBgKey, 0));
        }

        return preferences;
    }

    public static void save(@NonNull Context context, int widgetId, @NonNull MainWidgetPreferences preferences) throws InvalidConfigurationException {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        String command = preferences.getCommand();
        if (command == null) {
            throw new InvalidConfigurationException("Command is null");
        }
        editor.putString(generateKey(widgetId, KEY_COMMAND), command);

        if (preferences.getColorForeground() != null) {
            editor.putInt(generateKey(widgetId, KEY_COLOR_FOREGROUND), preferences.getColorForeground());
        } else {
            editor.remove(generateKey(widgetId, KEY_COLOR_FOREGROUND));
        }

        if (preferences.getColorBackground() != null) {
            editor.putInt(generateKey(widgetId, KEY_COLOR_BACKGROUND), preferences.getColorBackground());
        } else {
            editor.remove(generateKey(widgetId, KEY_COLOR_BACKGROUND));
        }

        editor.apply();
    }

    public static void delete(@NonNull Context context, int widgetId) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        editor.remove(generateKey(widgetId, KEY_COMMAND));
        editor.remove(generateKey(widgetId, KEY_COLOR_FOREGROUND));
        editor.remove(generateKey(widgetId, KEY_COLOR_BACKGROUND));

        editor.apply();
    }

    @NonNull
    private static String generateKey(int widgetId, @NonNull String valueName) {
        return widgetId + "_" + valueName;
    }

}
