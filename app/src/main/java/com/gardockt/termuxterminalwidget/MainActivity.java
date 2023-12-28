package com.gardockt.termuxterminalwidget;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.gardockt.termuxterminalwidget.shell.CommandRunnerService;
import com.gardockt.termuxterminalwidget.mainwidget.MainWidget;
import com.gardockt.termuxterminalwidget.util.RequestCodeManager;
import com.termux.shared.termux.TermuxConstants;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final ActivityResultLauncher<Intent> settingsActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::onAppSettingsActivityFinished
    );

    @RequiresApi(api = Build.VERSION_CODES.O)
    private final ServiceConnection commandRunnerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MainWidget.bindCommandRunnerService(MainActivity.this);
            MainActivity.this.unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRequiredPermissions();
    }

    /*
     * FLOW OF REQUESTING PERMISSIONS:
     *
     * 1. requestRequiredPermissions
     * 2. [any standard Android permissions were requested] onRequestPermissionsResult
     * 3. requestTermuxPermission
     * 4. [Termux RUN_COMMAND permission needed to be granted] onAppSettingsActivityFinished
     * 5. startCommandRunnerService
     */

    private void requestRequiredPermissions() {
        List<String> permissions = PermissionInfo.getRequiredDangerousPermissions();
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissions.toArray(new String[0]),
                    RequestCodeManager.getRequestCode()
            );
            // requestTermuxPermission is called inside callback (onRequestPermissionsResult)
        } else {
            requestTermuxPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.required_permission_denied, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        requestTermuxPermission();
    }

    private void requestTermuxPermission() {
        // launching Permissions activity directly is not possible
        // https://stackoverflow.com/q/32822101

        Runnable onCancel = () -> {
            Toast.makeText(this, R.string.required_permission_denied, Toast.LENGTH_SHORT).show();
            finish();
        };

        if (ActivityCompat.checkSelfPermission(this, TermuxConstants.PERMISSION_RUN_COMMAND) == PackageManager.PERMISSION_DENIED) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.permission_RUN_COMMAND_prompt)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        settingsActivityResultLauncher.launch(intent);
                    })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> onCancel.run())
                    .setOnCancelListener((dialog) -> onCancel.run())
                    .show();
        } else {
            startCommandRunnerService();
        }
    }

    private void onAppSettingsActivityFinished(ActivityResult result) {
        if (ActivityCompat.checkSelfPermission(this, TermuxConstants.PERMISSION_RUN_COMMAND) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, R.string.required_permission_denied, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startCommandRunnerService();
    }

    private void startCommandRunnerService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!CommandRunnerService.isRunning()) {
                Intent intent = new Intent(this, CommandRunnerService.class);
                startForegroundService(intent);

                // bind just to execute a function when the service starts
                bindService(intent, commandRunnerServiceConnection, BIND_ABOVE_CLIENT);
            }
        }
    }

}