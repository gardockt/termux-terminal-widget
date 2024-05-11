package com.gardockt.termuxterminalwidget;

import androidx.annotation.NonNull;

import java.util.Objects;

public class GlobalPreferences implements Cloneable {

    private int colorForeground;
    private int colorBackground;

    public GlobalPreferences(int colorForeground, int colorBackground) {
        this.colorForeground = colorForeground;
        this.colorBackground = colorBackground;
    }

    public int getColorForeground() {
        return colorForeground;
    }

    public void setColorForeground(int colorForeground) {
        this.colorForeground = colorForeground;
    }

    public int getColorBackground() {
        return colorBackground;
    }

    public void setColorBackground(int colorBackground) {
        this.colorBackground = colorBackground;
    }

    @NonNull
    @Override
    public GlobalPreferences clone() {
        try {
            GlobalPreferences clone = (GlobalPreferences) super.clone();
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
        return colorForeground == that.colorForeground &&
                colorBackground == that.colorBackground;
    }

    @Override
    public int hashCode() {
        return Objects.hash(colorForeground, colorBackground);
    }
}
