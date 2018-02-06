package projector.controller.song.util;

import projector.application.Settings;

public enum OrderMethod {
    ASCENDING_BY_TITLE("Ascending by title"),
    DESCENDING_BY_TITLE("Descending by title"),
    BY_MODIFIED_DATE("By modified date"),
    BY_PUBLISHED("By published");
    private String text;

    OrderMethod(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Settings.getInstance().getResourceBundle().getString(text);
    }
}
