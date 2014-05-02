package gui;

import javafx.scene.paint.Color;

public enum Status {
    NotConnected("Not connected", Color.RED),
    Connected("Connected. Waiting for companion", Color.YELLOW),
    Chatting("Chatting", Color.YELLOWGREEN),

    Error("Error", Color.RED);

    private final String value;
    private final Color color;

    private Status(String value, Color color) {
        this.value = value;
        this.color = color;
    }

    public String toString() {
        return value;
    }

    public Color toColor() {
        return color;
    }

    public static Status fromBoolean(boolean connected) {
        if (connected)
            return Status.Connected;
        else
            return Status.NotConnected;
    }
}