package com.gardockt.termuxterminalwidget;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gardockt.termuxterminalwidget.components.ColorButton;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

public class GlobalPreferencesFragment extends Fragment implements ColorPickerDialogListener {

    private static final String TAG = GlobalPreferencesFragment.class.getSimpleName();

    private ColorButton colorForegroundButton;
    private ColorButton colorBackgroundButton;

    private GlobalPreferences preferences;

    public GlobalPreferencesFragment() {
        super(R.layout.global_configure);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = GlobalPreferencesUtils.get(requireContext());

        colorForegroundButton = view.findViewById(R.id.color_foreground_button);
        colorBackgroundButton = view.findViewById(R.id.color_background_button);

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
        ColorScheme colorScheme = preferences.getColorScheme();

        switch (dialogId) {
            case ColorPickerDialogInvoker.COLOR_PICKER_CALLBACK_FOREGROUND:
                colorScheme.setColorForeground(color);
                colorForegroundButton.setColor(color);
                break;
            case ColorPickerDialogInvoker.COLOR_PICKER_CALLBACK_BACKGROUND:
                colorScheme.setColorBackground(color);
                colorBackgroundButton.setColor(color);
                break;
            default:
                Log.e(TAG, "onColorSelected attempted to handle unknown dialog ID: " + dialogId);
        }

        preferences.setColorScheme(colorScheme);
        GlobalPreferencesUtils.save(requireContext(), preferences);
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // intentionally left empty
    }
}
