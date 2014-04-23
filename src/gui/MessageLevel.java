package gui;

enum MessageLevel {
    Error("ERROR", "red"), Info("INFO", "gray"), Debug("DEBUG", "blue");

    String level;
    String htmlColor;

    MessageLevel(String level, String htmlColor) {
        this.level = level;
        this.htmlColor = htmlColor;
    }

    public String toString() {
        return level;
    }

    public String toHtmlColor() {
        return htmlColor;
    }
}