package com.gardockt.termuxterminalwidget.mainwidget;

import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.HashSet;
import java.util.Set;

// Rationale:
// On Android 8+, updating a widget requires a CommandRunnerService to be running. To determine
// whether an update is possible, we can check whether the service is running. While in most cases
// this is sufficient, there are some special cases when the service is started shortly after an
// update request - from my observations, this can be consistently reproduced by redeploying an app
// with widgets placed, which results in all widgets getting blanked. These special cases prove that
// update requests should be tracked independently of the service. To implement this idea, I have
// created MainWidgetUpdater, which can update the widget of given ID (in fact, it's just a wrapper
// for CommandRunnerService), and WidgetUpdateManager, which consumes update requests and to which a
// MainWidgetUpdater can be attached to. When requesting an update through WidgetUpdateManager, what
// action is done is determined by whether a WidgetUpdater is attached - if it is, the update is
// done immediately, and otherwise it is stored in a collection of pending updates.

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainWidgetUpdateManager {

    private final static String TAG = MainWidgetUpdateManager.class.getSimpleName();

    // Set is used, as there is no need to store order or duplicate requests
    private final Set<Integer> pendingUpdateWidgetIds = new HashSet<>();
    private MainWidgetUpdater widgetUpdater;

    // synchronization is required:
    // * HashSet is not thread-safe
    // * widgetUpdater may change to null after "widgetUpdater != null" condition passes through

    public synchronized void setWidgetUpdater(@Nullable MainWidgetUpdater widgetUpdater) {
        Log.d(TAG, "Setting widget updater to " + widgetUpdater);
        this.widgetUpdater = widgetUpdater;
    }

    @Nullable
    public synchronized MainWidgetUpdater getWidgetUpdater() {
        return widgetUpdater;
    }

    // runs an update immediately if widget updater is not null, otherwise adds widget ID to pending
    // updates collection
    public synchronized void addTask(int widgetId) {
        if (widgetUpdater != null) {
            Log.d(TAG, "Executing update task for widget ID " + widgetId);
            widgetUpdater.update(widgetId);
        } else {
            Log.d(TAG, "Adding widget ID " + widgetId + " to pending updates");
            pendingUpdateWidgetIds.add(widgetId);
        }
    }

    // theoretically, widgetUpdater may become invalid while inside the loop - this is not critical
    // though, as the worst that can possibly happen is that the widget simply will not get updated,
    // and with Android power-saving measures widget updates are not guaranteed anyway
    public synchronized void retryAll() {
        if (widgetUpdater == null) {
            return;
        }

        for (int widgetId : pendingUpdateWidgetIds) {
            Log.d(TAG, "Retrying update task for widget ID " + widgetId);
            widgetUpdater.update(widgetId);
        }
        pendingUpdateWidgetIds.clear();
    }

}
