package com.gardockt.termuxterminalwidget;

import androidx.annotation.NonNull;

import java.util.Objects;

public class GlobalPreferences implements Cloneable {

    private ColorScheme colorScheme = new ColorScheme(0xFFFFFFFF, 0xBF000000);
    private int textSizeSp = 14;

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(@NonNull ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public int getTextSizeSp() {
        return textSizeSp;
    }

    public void setTextSizeSp(int textSizeSp) {
        this.textSizeSp = textSizeSp;
    }

    @NonNull
    @Override
    public GlobalPreferences clone() {
        try {
            GlobalPreferences clone = (GlobalPreferences) super.clone();
            clone.setColorScheme(colorScheme.clone());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalPreferences that = (GlobalPreferences) o;
        return Objects.equals(colorScheme, that.colorScheme) &&
                textSizeSp == that.textSizeSp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                colorScheme,
                textSizeSp
        );
    }
}
