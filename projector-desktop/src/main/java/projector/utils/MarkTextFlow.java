package projector.utils;

import javafx.scene.text.TextFlow;

public class MarkTextFlow extends TextFlow {

    private boolean marker;

    public MarkTextFlow() {

    }

    public MarkTextFlow(TextFlow textFlow) {
        super(textFlow);
    }

    public boolean isMarker() {
        return marker;
    }

    public void setMarker(boolean marker) {
        this.marker = marker;
    }
}
