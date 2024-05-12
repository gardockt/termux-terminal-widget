package com.gardockt.termuxterminalwidget;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gardockt.termuxterminalwidget.components.ColorButton;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Locale;

public class GlobalPreferencesFragment extends Fragment implements ColorPickerDialogListener {

    private static final String TAG = GlobalPreferencesFragment.class.getSimpleName();

    private ColorButton colorForegroundButton;
    private ColorButton colorBackgroundButton;
    private EditText textSizeField;
    private Button saveButton;

    private GlobalPreferences preferences;

    public GlobalPreferencesFragment() {
        super(R.layout.global_configure);
    }

    private void save() {
        GlobalPreferences newPreferences = new GlobalPreferences();
        Context context = requireContext();

        // color scheme
        ColorScheme colorScheme = new ColorScheme(
                colorForegroundButton.getColor(),
                colorBackgroundButton.getColor()
        );
        newPreferences.setColorScheme(colorScheme);

        // text size
        try {
            int textSizeSp = Integer.parseInt(textSizeField.getText().toString());
            newPreferences.setTextSizeSp(textSizeSp);
        } catch (NumberFormatException ignored) {}

        GlobalPreferencesUtils.save(context, newPreferences);
        Toast.makeText(context, R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = GlobalPreferencesUtils.get(requireContext());

        colorForegroundButton = view.findViewById(R.id.color_foreground_button);
        colorBackgroundButton = view.findViewById(R.id.color_background_button);
        textSizeField = view.findViewById(R.id.field_text_size);
        saveButton = view.findViewById(R.id.save_button);

        colorForegroundButton.setOnClickListener(
                (v) -> ColorPickerDialogInvoker.showForegroundColorPicker(
                        getActivity(),
                        colorForegroundButton.getColor()
                )
        );

        colorBackgroundButton.setOnClickListener(
                (v) -> ColorPickerDialogInvoker.showBackgroundColorPicker(
                        getActivity(),
                        colorBackgroundButton.getColor()
                )
        );

        textSizeField.setText(
                String.format(Locale.getDefault(), "%d", preferences.getTextSizeSp())
        );

        saveButton.setOnClickListener((v) -> save());

        prepareColors();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        colorForegroundButton = null;
        colorBackgroundButton = null;
    }

    private void prepareColors() {
        ColorScheme colorScheme = preferences.getColorScheme();

        colorForegroundButton.setColor(colorScheme.getColorForeground());
        colorBackgroundButton.setColor(colorScheme.getColorBackground());
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
