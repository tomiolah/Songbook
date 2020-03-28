package projector.utils;

import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;

public class ContextMenuUtil {

    public static void initializeContextMenu(ContextMenu contextMenu, Logger LOG) {
        contextMenu.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            try {
                if (event.getButton() == MouseButton.SECONDARY) {
                    event.consume();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        contextMenu.setOnAction(event -> {
            try {
                contextMenu.hide();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }
}
