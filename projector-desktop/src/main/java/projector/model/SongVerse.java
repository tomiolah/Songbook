package projector.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;
import java.util.List;

import static projector.utils.StringUtils.stripAccents;

public class SongVerse extends BaseEntity {

    public static final String CHORUS = "[Chorus]";
    @Expose
    @DatabaseField(width = 1000)
    private String text;
    @Expose
    @DatabaseField(width = 1000)
    private String secondText;
    @DatabaseField(width = 1000)
    private String strippedText;
    @Expose
    @DatabaseField
    private boolean chorus;
    private boolean repeated;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private Song mainSong;

    public SongVerse() {
    }

    public SongVerse(SongVerse songVerse) {
        this.text = songVerse.text;
        if (text != null) {
            strippedText = stripAccents(text.toLowerCase());
        }
        this.chorus = songVerse.chorus;
        this.repeated = songVerse.repeated;
    }

    static List<SongVerse> cloneList(List<SongVerse> songVerses) {
        List<SongVerse> clonedSongVerses = new ArrayList<>(songVerses.size());
        for (SongVerse songVerse : songVerses) {
            clonedSongVerses.add(new SongVerse(songVerse));
        }
        return clonedSongVerses;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        strippedText = stripAccents(text.toLowerCase());
    }

    public boolean isChorus() {
        return chorus;
    }

    public void setChorus(boolean chorus) {
        this.chorus = chorus;
    }

    public boolean isRepeated() {
        return repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }

    public Song getMainSong() {
        return mainSong;
    }

    void setMainSong(Song mainSong) {
        this.mainSong = mainSong;
    }

    public String getStrippedText() {
        return strippedText;
    }

    public String getSecondText() {
        return secondText;
    }

    public void setSecondText(String secondText) {
        this.secondText = secondText;
    }
}
