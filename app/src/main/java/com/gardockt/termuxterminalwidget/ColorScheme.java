package com.gardockt.termuxterminalwidget;

import androidx.annotation.NonNull;

import java.util.Objects;

public class ColorScheme implements Cloneable {

    private int colorForeground;
    private int colorBackground;

    public ColorScheme(int colorForeground, int colorBackground) {
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
    public ColorScheme clone() {
        try {
            return (ColorScheme) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorScheme that = (ColorScheme) o;
        return colorForeground == that.colorForeground &&
                colorBackground == that.colorBackground;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                colorForeground,
                colorBackground
        );
    }
}
