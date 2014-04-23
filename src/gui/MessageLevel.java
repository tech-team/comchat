package gui;

enum MessageLevel {
    Error("red", "ERROR"), Info("gray", "INFO");

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