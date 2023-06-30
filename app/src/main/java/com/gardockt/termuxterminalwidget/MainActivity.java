package com.gardockt.termuxterminalwidget;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gardockt.termuxterminalwidget.shell.CommandRunner;

// NOTE: ideally TerminalView from Termux should be used, however it does not seem to be able to
//       just print given text, and running sessions with non-Termux apps seems to be impossible due
//       to "Access denied" errors (as a result of attempting to access Termux data directory)

public class MainActivity extends AppCompatActivity {

    private EditText commandField;
    private Button button;
    private TextView outputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        commandField = findViewById(R.id.commandField);
        outputText = findViewById(R.id.outputText);

        button.setOnClickListener((e) -> onClick());
    }

    private void onClick() {
        String command = commandField.getText().toString();
        CommandRunner.runCommand(this, command, this::onCommandFinished);
    }

    // This is not going to be accurate. See the note above this class.
    private String parseOutput(String output) {
        // remove ANSI escape sequences
        return output.replaceAll("\\e\\[[\\d;?]*[a-zA-Z]", "");
    }

    private void onCommandFinished(int exitCode, String stdout, String stderr) {
        String parsedStdout = parseOutput(stdout);
        runOnUiThread(() -> outputText.setText(parsedStdout));
    }

}