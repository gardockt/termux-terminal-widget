package com.gardockt.termuxterminalwidget.mainwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MainWidgetUpdateWorker extends Worker {

    private final AppWidgetManager widgetManager;

    public MainWidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.widgetManager = AppWidgetManager.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int widgetId = getInputData().getInt(MainWidget.EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return Result.failure();
        }

        MainWidget.updateWidget(context, widgetManager, widgetId, true);
        return Result.success();
    }
}
