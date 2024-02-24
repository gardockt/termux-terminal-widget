package com.gardockt.termuxterminalwidget;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;

public class ColorPickerDialogInvoker {

    public static final int COLOR_PICKER_CALLBACK_FOREGROUND = 0;
    public static final int COLOR_PICKER_CALLBACK_BACKGROUND = 1;

    public static void showForegroundColorPicker(FragmentActivity fragmentActivity, int initialColor) {
        buildDialog(COLOR_PICKER_CALLBACK_FOREGROUND, initialColor).show(fragmentActivity);
    }

    public static void showBackgroundColorPicker(FragmentActivity fragmentActivity, int initialColor) {
        buildDialog(COLOR_PICKER_CALLBACK_BACKGROUND, initialColor).show(fragmentActivity);
    }

    @NonNull
    private static ColorPickerDialog.Builder buildDialog(int dialogId, int initialColor) {
        return ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setShowAlphaSlider(true)
                .setColor(initialColor)
                .setDialogId(dialogId);
    }
}
