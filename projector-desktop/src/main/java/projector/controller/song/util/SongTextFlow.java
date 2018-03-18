package projector.controller.song.util;

import javafx.scene.text.TextFlow;
import projector.model.Song;

public abstract class SongTextFlow {
    private Song song;
    private TextFlow textFlow;

    SongTextFlow(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }

    public void setTextFlow(TextFlow textFlow) {
        this.textFlow = textFlow;
    }
}
