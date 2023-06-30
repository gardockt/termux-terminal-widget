package com.gardockt.termuxterminalwidget.shell;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.termux.shared.termux.TermuxConstants;

public class PluginResultsService extends IntentService {

    private static final String TAG = PluginResultsService.class.getSimpleName();

    public PluginResultsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle resultBundle = intent.getBundleExtra(TermuxConstants.TERMUX_APP.TERMUX_SERVICE.EXTRA_PLUGIN_RESULT_BUNDLE);
        if (resultBundle == null) {
            Log.e(TAG, "Missing result bundle");
            return;
        }

        ResultReceiver resultReceiver = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);
        resultReceiver.send(0, resultBundle);
    }

}
