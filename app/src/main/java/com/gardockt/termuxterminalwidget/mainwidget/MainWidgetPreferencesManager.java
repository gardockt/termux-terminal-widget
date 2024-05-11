package com.gardockt.termuxterminalwidget.mainwidget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.gardockt.termuxterminalwidget.ColorScheme;
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

        boolean colorSchemeSet = true;

        int colorForeground = 0;
        String colorFgKey = generateKey(widgetId, KEY_COLOR_FOREGROUND);
        if (sharedPreferences.contains(colorFgKey)) {
            colorForeground = sharedPreferences.getInt(colorFgKey, 0);
        } else {
            colorSchemeSet = false;
        }

        int colorBackground = 0;
        String colorBgKey = generateKey(widgetId, KEY_COLOR_BACKGROUND);
        if (sharedPreferences.contains(colorBgKey)) {
            colorBackground = sharedPreferences.getInt(colorBgKey, 0);
        } else {
            colorSchemeSet = false;
        }

        if (colorSchemeSet) {
            ColorScheme colorScheme = new ColorScheme(colorForeground, colorBackground);
            preferences.setColorScheme(colorScheme);
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

        ColorScheme colorScheme = preferences.getColorScheme();
        if (colorScheme != null) {
            editor.putInt(generateKey(widgetId, KEY_COLOR_FOREGROUND), colorScheme.getColorForeground());
            editor.putInt(generateKey(widgetId, KEY_COLOR_BACKGROUND), colorScheme.getColorBackground());
        } else {
            editor.remove(generateKey(widgetId, KEY_COLOR_FOREGROUND));
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
