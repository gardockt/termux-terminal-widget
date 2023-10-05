package com.gardockt.termuxterminalwidget;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.gardockt.termuxterminalwidget.shell.CommandRunnerService;
import com.gardockt.termuxterminalwidget.mainwidget.MainWidget;

public class MainActivity extends AppCompatActivity {

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