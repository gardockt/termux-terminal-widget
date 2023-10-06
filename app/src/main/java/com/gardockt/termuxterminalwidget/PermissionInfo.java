package com.gardockt.termuxterminalwidget;

import android.Manifest;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PermissionInfo {

    // Returns required permissions, which are considered dangerous on Android version on which the
    // app is running, or an empty list if there are not any. Only standard Android permissions are
    // returned - custom permissions, such as Termux RUN_COMMAND, are omitted.
    @NonNull
    public static List<String> getRequiredDangerousPermissions() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        return permissions;
    }

}
