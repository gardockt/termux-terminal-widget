package com.gardockt.termuxterminalwidget.mainwidget;

import com.gardockt.termuxterminalwidget.ColorScheme;

public class MainWidgetPreferences {

    private String command;
    private ColorScheme colorScheme = null;

    public MainWidgetPreferences(String command) {
        this.command = command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }
}
