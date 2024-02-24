package com.gardockt.termuxterminalwidget.mainwidget;

public class MainWidgetPreferences {

    private String command;
    private Integer colorForeground = null;
    private Integer colorBackground = null;

    public MainWidgetPreferences(String command) {
        this.command = command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public Integer getColorForeground() {
        return colorForeground;
    }

    public void setColorForeground(Integer colorForeground) {
        this.colorForeground = colorForeground;
    }

    public Integer getColorBackground() {
        return colorBackground;
    }

    public void setColorBackground(Integer colorBackground) {
        this.colorBackground = colorBackground;
    }
}
