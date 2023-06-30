package com.gardockt.termuxterminalwidget;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.gardockt.termuxterminalwidget.shell.CommandRunner;

public class MainActivity extends AppCompatActivity {

    private EditText commandField;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        commandField = findViewById(R.id.commandField);

        button.setOnClickListener((e) -> onClick());
    }

    private void onClick() {
        CommandRunner.runCommand(this, commandField.getText().toString());
    }

}