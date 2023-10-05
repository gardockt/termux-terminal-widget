package com.gardockt.termuxterminalwidget.mainwidget;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.gardockt.termuxterminalwidget.R;
import com.gardockt.termuxterminalwidget.shell.CommandRunnerService;
import com.gardockt.termuxterminalwidget.exceptions.InvalidConfigurationException;
import com.gardockt.termuxterminalwidget.util.TriConsumer;

// Rationale: see MainWidgetUpdateManager

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainWidgetUpdater {

    private static final String TAG = MainWidgetUpdater.class.getSimpleName();

    private final CommandRunnerService commandRunnerService;
    private final TriConsumer<Context, Integer, String> onCommandFinished;

    public MainWidgetUpdater(@NonNull CommandRunnerService commandRunnerService, @NonNull TriConsumer<Context, Integer, String> onCommandFinished) {
        this.commandRunnerService = commandRunnerService;
        this.onCommandFinished = onCommandFinished;
    }

    public void update(int widgetId) {
        try {
            MainWidgetPreferences preferences = MainWidgetPreferencesManager.load(commandRunnerService, widgetId);
            commandRunnerService.runCommand(preferences.getCommand(), (exitCode, stdout, stderr) -> onCommandFinished.accept(commandRunnerService, widgetId, stdout));
        } catch (InvalidConfigurationException ex) {
            Log.e(TAG, String.format("%s: %s (%s)", commandRunnerService.getString(R.string.error_updating_preferences), ex.getClass().getSimpleName(), ex.getMessage()));
        }
    }

}
