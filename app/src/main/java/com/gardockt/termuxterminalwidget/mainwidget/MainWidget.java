package com.gardockt.termuxterminalwidget.mainwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.gardockt.termuxterminalwidget.GlobalPreferencesUtils;
import com.gardockt.termuxterminalwidget.R;
import com.gardockt.termuxterminalwidget.util.RequestCodeManager;
import com.gardockt.termuxterminalwidget.util.TriConsumer;
import com.gardockt.termuxterminalwidget.shell.CommandRunner;
import com.gardockt.termuxterminalwidget.shell.CommandRunnerService;
import com.gardockt.termuxterminalwidget.exceptions.InvalidConfigurationException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// NOTE: ideally TerminalView from Termux should be used, however it does not seem to be able to
//       just print given text, and running sessions with non-Termux apps seems to be impossible due
//       to "Access denied" errors (as a result of attempting to access Termux data directory)

public class MainWidget extends AppWidgetProvider {

    private static final String TAG = MainWidget.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static final MainWidgetUpdateManager widgetUpdateManager = new MainWidgetUpdateManager();

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static final ServiceConnection commandRunnerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Service connected");
            CommandRunnerService.CommandRunnerServiceBinder binder = (CommandRunnerService.CommandRunnerServiceBinder) service;
            CommandRunnerService commandRunnerService = binder.getService();
            MainWidgetUpdater widgetUpdater = new MainWidgetUpdater(commandRunnerService, onCommandFinished);
            widgetUpdateManager.setWidgetUpdater(widgetUpdater);
            widgetUpdateManager.retryAll();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    private final static TriConsumer<Context, Integer, String> onCommandFinished = (context, widgetId, stdout) -> {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = getInitializedRemoteViews(context);
        views.setOnClickPendingIntent(R.id.text, onClickPendingIntent(context, widgetId));
        views.setTextViewText(R.id.text, parseOutput(stdout));
        appWidgetManager.updateAppWidget(widgetId, views);
    };

    private final static Map<Integer, SharedPreferences.OnSharedPreferenceChangeListener> globalPreferenceChangeListenersByWidgetId = new HashMap<>();

    public final static String ACTION_CLICK = "click";
    public final static String EXTRA_WIDGET_ID = "widget_id";

    public static void updateWidget(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int widgetId, boolean silent) {
        Log.d(TAG, "updateAppWidget");

        if (!globalPreferenceChangeListenersByWidgetId.containsKey(widgetId)) {
            Log.d(TAG, "Registering global preference change listener for widget ID " + widgetId);
            SharedPreferences.OnSharedPreferenceChangeListener listener =
                    (preferences, key) -> onGlobalPreferenceChanged(context, preferences, key, widgetId);
            GlobalPreferencesUtils.getSharedPreferences(context)
                    .registerOnSharedPreferenceChangeListener(listener);
            globalPreferenceChangeListenersByWidgetId.put(widgetId, listener);
        }

        // set on-click action outside command callback, so that it will be there even if the update
        // fails
        RemoteViews views = getInitializedRemoteViews(context);
        views.setOnClickPendingIntent(R.id.text, onClickPendingIntent(context, widgetId));
        appWidgetManager.updateAppWidget(widgetId, views);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            widgetUpdateManager.addTask(widgetId);
            if (!bindCommandRunnerService(context)) {
                Log.w(TAG, "Cannot update the widget - service is not running");
                if (!silent) {
                    Toast.makeText(context, R.string.cannot_update_widget_service_not_running, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            MainWidgetPreferences preferences;
            try {
                preferences = MainWidgetPreferencesManager.load(context, widgetId);
            } catch (InvalidConfigurationException ex) {
                Log.e(TAG, String.format("Error while updating preferences: %s (%s)", ex.getClass().getSimpleName(), ex.getMessage()));
                // while a critical error, we are still displaying it conditionally, as onUpdate is
                // called when a widget is added, before the configuration activity is closed, so no
                // valid configuration should be expected at that point
                if (!silent) {
                    Toast.makeText(context, String.format("%s: %s (%s)", context.getString(R.string.error_updating_preferences), ex.getClass().getSimpleName(), ex.getMessage()), Toast.LENGTH_LONG).show();
                }
                return;
            }

            String command = preferences.getCommand();
            CommandRunner.runCommand(context, command, (exitCode, stdout, stderr) -> onCommandFinished.accept(context, widgetId, stdout));
        }
    }

    // Returns RemoteViews object representing the widget, initialized with global settings.
    @NonNull
    private static RemoteViews getInitializedRemoteViews(@NonNull Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);
        SharedPreferences globalPreferences = GlobalPreferencesUtils.getSharedPreferences(context);
        String[] keysToUpdate = {
                GlobalPreferencesUtils.KEY_DEFAULT_COLOR_FOREGROUND,
                GlobalPreferencesUtils.KEY_DEFAULT_COLOR_BACKGROUND,
        };

        for (String key : keysToUpdate) {
            updateRemoteViewsByGlobalPreferencesKey(context, globalPreferences, key, views);
        }

        return views;
    }

    private static void onGlobalPreferenceChanged(
            @NonNull Context context,
            @NonNull SharedPreferences globalPreferences,
            @NonNull String key,
            int widgetId
    ) {
        Log.d(TAG, String.format("Widget %d: Global preferences changed for key %s", widgetId, key));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_widget);
        updateRemoteViewsByGlobalPreferencesKey(context, globalPreferences, key, views);
        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views);
    }

    private static void updateRemoteViewsByGlobalPreferencesKey(
            @NonNull Context context,
            @NonNull SharedPreferences globalPreferences,
            @NonNull String key,
            @NonNull RemoteViews views
    ) {
        switch (key) {
            case GlobalPreferencesUtils.KEY_DEFAULT_COLOR_FOREGROUND:
                int defaultColorFg = context.getColor(R.color.widget_default_color_foreground);
                int colorFg = globalPreferences.getInt(key, defaultColorFg);
                Log.d(TAG, String.format("Setting foreground color to #%8X", colorFg));
                views.setInt(R.id.text, "setTextColor", colorFg);
                break;
            case GlobalPreferencesUtils.KEY_DEFAULT_COLOR_BACKGROUND:
                int defaultColorBg = context.getColor(R.color.widget_default_color_background);
                int colorBg = globalPreferences.getInt(key, defaultColorBg);
                Log.d(TAG, String.format("Setting background color to #%8X", colorBg));
                views.setInt(R.id.text, "setBackgroundColor", colorBg);
                break;
            default:
                Log.e(TAG, String.format("Updating RemoteViews by global preferences key %s is not implemented", key));
        }
    }

    // This is not going to be accurate. See the note above MainWidget.
    @NonNull
    private static String parseOutput(@Nullable String output) {
        if (output == null) {
            return "";
        }

        // remove ANSI escape sequences
        return output.replaceAll("\\e\\[[\\d;?]*[a-zA-Z]", "");
    }

    @NonNull
    private static PendingIntent onClickPendingIntent(Context context, int widgetId) {
        Intent intent = new Intent(context, MainWidget.class);
        intent.setAction(ACTION_CLICK);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(context, RequestCodeManager.getRequestCode(), intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bindCommandRunnerService(context);
        }
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, true);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Log.d(TAG, "onRestored");
        // TODO
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    @Override
    public void onDeleted(@NonNull Context context, @NonNull int[] widgetIds) {
        Log.d(TAG, "onDeleted");
        for (int widgetId : widgetIds) {
            MainWidgetPreferencesManager.delete(context, widgetId);
            cancelUpdateWork(context, widgetId);

            SharedPreferences.OnSharedPreferenceChangeListener listener = globalPreferenceChangeListenersByWidgetId.get(widgetId);
            if (listener != null) {
                GlobalPreferencesUtils.getSharedPreferences(context)
                        .unregisterOnSharedPreferenceChangeListener(listener);
                globalPreferenceChangeListenersByWidgetId.remove(widgetId);
            }
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Log.d(TAG, "onReceive");
        super.onReceive(context, intent);

        if (ACTION_CLICK.equals(intent.getAction())) {
            Log.d(TAG, "Click event received, refreshing widget");
            Bundle bundle = intent.getExtras();
            int widgetId = bundle.getInt(EXTRA_WIDGET_ID);
            updateWidget(context, AppWidgetManager.getInstance(context), widgetId, false);
        }
    }

    // returns true if binding intent is sent or the service is already bound
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean bindCommandRunnerService(@NonNull Context context) {
        if (widgetUpdateManager.getWidgetUpdater() != null) {
            // service already bound
            return true;
        }

        Log.d(TAG, "Attempting to bind CommandRunnerService");
        if (!CommandRunnerService.isRunning()) {
            // foreground service is required, and we can't start one here - abort the bind
            Log.d(TAG, "CommandRunnerService is not running, aborting");
            return false;
        }

        Log.d(TAG, "Sending bind intent");
        Intent intent = new Intent(context, CommandRunnerService.class);
        context.getApplicationContext().bindService(intent, commandRunnerServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT);
        return true;
    }

    static void createWidget(@NonNull Context context, int widgetId, @NonNull MainWidgetPreferences preferences) throws InvalidConfigurationException {
        MainWidgetPreferencesManager.save(context, widgetId, preferences);

        // enqueue update work
        resetUpdateWork(context, widgetId);
    }

    // cancels update work if one exists, and enqueues a new one; use this method instead of just
    // adding to ensure that only one such job exists
    private static void resetUpdateWork(@NonNull Context context, int widgetId) {
        WorkManager workManager = WorkManager.getInstance(context);
        long updateIntervalMinutes = 15;

        cancelUpdateWork(context, widgetId);
        Data workData = new Data.Builder()
                .putInt(MainWidget.EXTRA_WIDGET_ID, widgetId)
                .build();
        WorkRequest workRequest = new PeriodicWorkRequest.Builder(MainWidgetUpdateWorker.class, updateIntervalMinutes, TimeUnit.MINUTES)
                .setInputData(workData)
                .addTag(MainWidget.getUpdateJobTag(widgetId))
                .build();
        workManager.enqueue(workRequest);
    }

    private static void cancelUpdateWork(@NonNull Context context, int widgetId) {
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.cancelAllWorkByTag(MainWidget.getUpdateJobTag(widgetId));
    }

    @NonNull
    public static String getUpdateJobTag(int widgetId) {
        return Integer.toString(widgetId);
    }

}