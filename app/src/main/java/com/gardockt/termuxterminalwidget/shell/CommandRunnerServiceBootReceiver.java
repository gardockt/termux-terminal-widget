package com.gardockt.termuxterminalwidget.shell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.gardockt.termuxterminalwidget.PermissionInfo;

import java.util.List;

public class CommandRunnerServiceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                List<String> requiredPermissions = PermissionInfo.getRequiredDangerousPermissions();
                if (requiredPermissions.stream().allMatch(
                        perm -> ActivityCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
                )) {
                    Intent serviceIntent = new Intent(context, CommandRunnerService.class);
                    context.startForegroundService(serviceIntent);
                }
            }
        }
    }

}
