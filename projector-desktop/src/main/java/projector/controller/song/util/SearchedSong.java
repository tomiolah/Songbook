package projector.controller.song.util;

import javafx.scene.text.TextFlow;
import projector.model.Song;

public class SearchedSong {

    private String foundAtVerse;
    private Song song;
    private TextFlow textFlow;

    public SearchedSong(Song song) {
        this.song = song;
    }

    public String getFoundAtVerse() {
        return foundAtVerse;
    }

    public void setFoundAtVerse(String foundAtVerse) {
        this.foundAtVerse = foundAtVerse;
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
