package com.gardockt.termuxterminalwidget.mainwidget;

public class MainWidgetPreferences {

    private String command;

    public MainWidgetPreferences(String command) {
        this.command = command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

}
