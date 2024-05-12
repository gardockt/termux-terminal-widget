package com.gardockt.termuxterminalwidget.mainwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.gardockt.termuxterminalwidget.ColorPickerDialogInvoker;
import com.gardockt.termuxterminalwidget.ColorScheme;
import com.gardockt.termuxterminalwidget.GlobalPreferences;
import com.gardockt.termuxterminalwidget.GlobalPreferencesUtils;
import com.gardockt.termuxterminalwidget.R;
import com.gardockt.termuxterminalwidget.components.ColorButton;
import com.gardockt.termuxterminalwidget.databinding.MainWidgetConfigureBinding;
import com.gardockt.termuxterminalwidget.exceptions.InvalidConfigurationException;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Locale;

public class MainWidgetConfigureActivity extends AppCompatActivity implements ColorPickerDialogListener {

    private static final String TAG = MainWidgetConfigureActivity.class.getSimpleName();

    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private EditText commandField;
    private SwitchCompat customColorsSwitch;
    private LinearLayout customColorsLayout;
    private ColorButton colorForegroundButton;
    private ColorButton colorBackgroundButton;
    private EditText textSizeField;

    private final View.OnClickListener onConfirmButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = MainWidgetConfigureActivity.this;

            // command
            String command = commandField.getText().toString();

            MainWidgetPreferences preferences = new MainWidgetPreferences(command);

            // color scheme
            if (customColorsSwitch.isChecked()) {
                ColorScheme colorScheme = new ColorScheme(
                        colorForegroundButton.getColor(),
                        colorBackgroundButton.getColor()
                );
                preferences.setColorScheme(colorScheme);
            }

            // text size
            String textSizeString = textSizeField.getText().toString();
            try {
                preferences.setTextSizeSp(Integer.parseInt(textSizeString));
            } catch (NumberFormatException ignored) {}

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

        customColorsLayout = binding.customColorsLayout;
        customColorsSwitch = binding.customColorsSwitch;
        customColorsSwitch.setOnCheckedChangeListener((view, checked) -> {
            customColorsLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
        });

        colorForegroundButton = binding.colorForegroundButton;
        colorForegroundButton.setOnClickListener(
                (view) -> ColorPickerDialogInvoker.showForegroundColorPicker(
                        this,
                        colorForegroundButton.getColor()
                )
        );

        colorBackgroundButton = binding.colorBackgroundButton;
        colorBackgroundButton.setOnClickListener(
                (view) -> ColorPickerDialogInvoker.showBackgroundColorPicker(
                        this,
                        colorBackgroundButton.getColor()
                )
        );

        textSizeField = binding.fieldTextSize;

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

        fillSettings(preferences);
    }

    private void fillSettings(@NonNull MainWidgetPreferences widgetPreferences) {
        GlobalPreferences globalPreferences = GlobalPreferencesUtils.get(this);

        // command
        commandField.setText(widgetPreferences.getCommand());

        // color scheme
        ColorScheme colorScheme = widgetPreferences.getColorScheme();
        boolean customColorsEnabled = (colorScheme != null);

        if (!customColorsEnabled) {
            colorScheme = globalPreferences.getColorScheme();
        }

        colorForegroundButton.setColor(colorScheme.getColorForeground());
        colorBackgroundButton.setColor(colorScheme.getColorBackground());

        customColorsSwitch.setChecked(customColorsEnabled);

        // text size
        Integer textSize = widgetPreferences.getTextSizeSp();
        int globalTextSize = globalPreferences.getTextSizeSp();

        if (textSize != null) {
            textSizeField.setText(String.format(Locale.getDefault(), "%d", textSize));
        }
        textSizeField.setHint(String.format(Locale.getDefault(), "%d", globalTextSize));
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case ColorPickerDialogInvoker.COLOR_PICKER_CALLBACK_FOREGROUND:
                colorForegroundButton.setColor(color);
                break;
            case ColorPickerDialogInvoker.COLOR_PICKER_CALLBACK_BACKGROUND:
                colorBackgroundButton.setColor(color);
                break;
            default:
                Log.e(TAG, "onColorSelected attempted to handle unknown dialog ID: " + dialogId);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // intentionally left empty
    }
}
