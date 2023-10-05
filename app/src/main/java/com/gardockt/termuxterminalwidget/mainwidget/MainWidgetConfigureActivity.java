package com.gardockt.termuxterminalwidget.mainwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gardockt.termuxterminalwidget.R;
import com.gardockt.termuxterminalwidget.databinding.MainWidgetConfigureBinding;
import com.gardockt.termuxterminalwidget.exceptions.InvalidConfigurationException;

public class MainWidgetConfigureActivity extends AppCompatActivity {

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText commandField;
    private final View.OnClickListener onConfirmButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MainWidgetConfigureActivity.this;

            String command = commandField.getText().toString();

            MainWidgetPreferences preferences = new MainWidgetPreferences(command);

            try {
                MainWidget.createWidget(context, widgetId, preferences);

                // Make sure we pass back the original widgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            } catch (InvalidConfigurationException ex) {
                Toast.makeText(context, String.format("%s: %s (%s)", context.getString(R.string.error_creating_widget), ex.getClass().getSimpleName(), ex.getMessage()), Toast.LENGTH_LONG).show();
            }
        }
    };
    private MainWidgetConfigureBinding binding;

    public MainWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        binding = MainWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        commandField = binding.commandField;
        binding.confirmButton.setOnClickListener(onConfirmButtonClickListener);

        // word wrapping without allowing newline character; cannot be done in XML
        // https://stackoverflow.com/a/31683560
        commandField.setMaxLines(Integer.MAX_VALUE);
        commandField.setHorizontallyScrolling(false);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        MainWidgetPreferences preferences;
        try {
            preferences = MainWidgetPreferencesManager.load(this, widgetId);
        } catch (InvalidConfigurationException ex) {
            preferences = new MainWidgetPreferences("");
        }

        commandField.setText(preferences.getCommand());
    }
}