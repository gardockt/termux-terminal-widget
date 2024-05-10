package com.gardockt.termuxterminalwidget;

import android.content.Context;
import android.content.SharedPreferences;
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

    private SharedPreferences preferences;

    public GlobalPreferencesFragment() {
        super(R.layout.global_configure);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = GlobalPreferencesUtils.getSharedPreferences(requireContext());

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
        Context context = requireContext();

        int defaultColorFg = context.getColor(R.color.widget_default_color_foreground);
        int defaultColorBg = context.getColor(R.color.widget_default_color_background);

        int colorFg = preferences.getInt(
                GlobalPreferencesUtils.KEY_DEFAULT_COLOR_FOREGROUND,
                defaultColorFg
        );
        int colorBg = preferences.getInt(
                GlobalPreferencesUtils.KEY_DEFAULT_COLOR_BACKGROUND,
                defaultColorBg
        );

        colorForegroundButton.setColor(colorFg);
        colorBackgroundButton.setColor(colorBg);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        SharedPreferences.Editor editor = preferences.edit();

        switch (dialogId) {
            case ColorPickerDialogInvoker.COLOR_PICKER_CALLBACK_FOREGROUND:
                editor.putInt(GlobalPreferencesUtils.KEY_DEFAULT_COLOR_FOREGROUND, color);
                colorForegroundButton.setColor(color);
                break;
            case ColorPickerDialogInvoker.COLOR_PICKER_CALLBACK_BACKGROUND:
                editor.putInt(GlobalPreferencesUtils.KEY_DEFAULT_COLOR_BACKGROUND, color);
                colorBackgroundButton.setColor(color);
                break;
            default:
                Log.e(TAG, "onColorSelected attempted to handle unknown dialog ID: " + dialogId);
        }

        editor.apply();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        // intentionally left empty
    }
}
