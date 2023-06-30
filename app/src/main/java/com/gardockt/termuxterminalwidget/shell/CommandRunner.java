package com.gardockt.termuxterminalwidget.shell;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gardockt.termuxterminalwidget.util.RequestCodeManager;
import com.gardockt.termuxterminalwidget.util.TriConsumer;
import com.termux.shared.termux.TermuxConstants;

public class CommandRunner {

    private final static String TAG = CommandRunner.class.getSimpleName();

    public static void runCommand(@NonNull Context context, @NonNull String command, @NonNull TriConsumer<Integer, String, String> onCommandFinished) {
        Log.d(TAG, "runCommand");

        int requestCode = RequestCodeManager.getRequestCode();

        Intent resultIntent = new Intent(context, PluginResultsService.class);
        ResultReceiver resultReceiver = new PluginResultReceiver(onCommandFinished);
        resultIntent.putExtra(Intent.EXTRA_RESULT_RECEIVER, receiverForSending(resultReceiver));

        int pendingIntentFlags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentFlags |= PendingIntent.FLAG_MUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getService(context, requestCode, resultIntent, pendingIntentFlags);

        Intent intent = new Intent();
        intent.setClassName(TermuxConstants.TERMUX_PACKAGE_NAME, TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE_NAME);
        intent.setAction(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.ACTION_RUN_COMMAND);
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_COMMAND_PATH, TermuxConstants.TERMUX_FILES_DIR_PATH + "/usr/bin/sh");
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_ARGUMENTS, new String[]{"-c", command});
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_BACKGROUND, true);
        intent.putExtra(TermuxConstants.TERMUX_APP.RUN_COMMAND_SERVICE.EXTRA_PENDING_INTENT, pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    // https://stackoverflow.com/questions/5743485/android-resultreceiver-across-packages#12183036
    private static ResultReceiver receiverForSending(@NonNull ResultReceiver originalReceiver) {
        Parcel parcel = Parcel.obtain();
        originalReceiver.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        ResultReceiver modifiedReceiver = ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return modifiedReceiver;
    }

    private static class PluginResultReceiver extends ResultReceiver {
        private final TriConsumer<Integer, String, String> onCommandFinished;

        public PluginResultReceiver(TriConsumer<Integer, String, String> onCommandFinished) {
            super(null);
            this.onCommandFinished = onCommandFinished;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            int exitCode   = resultData.getInt(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_EXIT_CODE);
            String stdout  = resultData.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDOUT);
            String stderr  = resultData.getString(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE_STDERR);
            onCommandFinished.accept(exitCode, stdout, stderr);
        }
    }

}
