package projector.utils;

import javafx.scene.paint.Color;

public class ColorUtil {

    public static Color getCollectionNameColor() {
        return Color.rgb(224, 247, 250);
    }

    public static Color getSongTitleColor() {
        return getGeneralTextColor();
    }

    public static Color getGeneralTextColor() {
        return Color.rgb(255, 255, 255);
    }

    public static Color getReferenceTextColor() {
        return Color.rgb(200, 224, 255);
    }

    public static Color getVisitedTextColor() {
        return Color.rgb(187, 148, 255);
    }
}
