package com.gardockt.termuxterminalwidget.mainwidget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.gardockt.termuxterminalwidget.exceptions.InvalidConfigurationException;

public class MainWidgetPreferencesManager {

    private static final String PREFS_NAME = "MainWidget";
    private static final String VALUE_COMMAND = "command";

    @NonNull
    public static MainWidgetPreferences load(@NonNull Context context, int widgetId) throws InvalidConfigurationException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String command = sharedPreferences.getString(generateKey(widgetId, VALUE_COMMAND), null);
        if (command == null) {
            throw new InvalidConfigurationException("Command is null");
        }

        return new MainWidgetPreferences(command);
    }

    public static void save(@NonNull Context context, int widgetId, @NonNull MainWidgetPreferences preferences) throws InvalidConfigurationException {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        String command = preferences.getCommand();
        if (command == null) {
            throw new InvalidConfigurationException("Command is null");
        }
        editor.putString(generateKey(widgetId, VALUE_COMMAND), command);

        editor.apply();
    }

    public static void delete(@NonNull Context context, int widgetId) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

        editor.remove(generateKey(widgetId, VALUE_COMMAND));

        editor.apply();
    }

    @NonNull
    private static String generateKey(int widgetId, @NonNull String valueName) {
        return widgetId + "_" + valueName;
    }

}
