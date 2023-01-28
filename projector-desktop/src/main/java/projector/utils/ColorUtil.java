package projector.utils;

import javafx.scene.paint.Color;
import projector.application.Settings;

public class ColorUtil {

    private static final Settings settings = Settings.getInstance();

    public static Color getCollectionNameColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(224, 247, 250);
        } else {
            return Color.rgb(34, 42, 116);
        }
    }

    public static Color getSongTitleColor() {
        return getGeneralTextColor();
    }

    public static Color getGeneralTextColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(255, 255, 255);
        } else {
            return Color.rgb(0, 0, 0);
        }
    }

    public static Color getReferenceTextColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(200, 224, 255);
        } else {
            return Color.rgb(24, 24, 24);
        }
    }

    public static Color getVisitedTextColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(187, 148, 255);
        } else {
            return Color.rgb(56, 0, 129);
        }
    }
}
