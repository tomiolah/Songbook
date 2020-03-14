package projector.model;

import com.bence.projector.common.model.SectionType;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;
import projector.application.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static projector.utils.StringUtils.stripAccents;

public class SongVerse extends BaseEntity {

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
    @DatabaseField
    private Integer sectionTypeData;
    private SectionType sectionType;

    public SongVerse() {
    }

    public SongVerse(SongVerse songVerse) {
        this.text = songVerse.text;
        if (text != null) {
            strippedText = stripAccents(text.toLowerCase());
        }
        this.chorus = songVerse.chorus;
        this.repeated = songVerse.repeated;
        this.secondText = songVerse.secondText;
        this.mainSong = songVerse.mainSong;
        this.sectionTypeData = songVerse.sectionTypeData;
        this.sectionType = songVerse.sectionType;
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

    public void setMainSong(Song mainSong) {
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

    public SectionType getSectionType() {
        if (sectionTypeData == null) {
            sectionType = SectionType.VERSE;
        } else {
            sectionType = SectionType.getInstance(sectionTypeData);
        }
        if (isChorus()) {
            sectionType = SectionType.CHORUS;
        }
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
        this.sectionTypeData = sectionType.getValue();
    }

    private String getSectionTypeString() {
        Settings settings = Settings.getInstance();
        ResourceBundle bundle = settings.getResourceBundle();
        switch (getSectionType()) {
            case INTRO:
                return bundle.getString("letter_intro");
            case VERSE:
                return bundle.getString("letter_verse");
            case PRE_CHORUS:
                return bundle.getString("letter_pre_chorus");
            case CHORUS:
                return bundle.getString("letter_chorus");
            case BRIDGE:
                return bundle.getString("letter_bridge");
            case CODA:
                return bundle.getString("letter_coda");
        }
        return "";
    }

    private int getSongVerseCountBySectionType(SectionType sectionType) {
        if (mainSong == null) {
            return 0;
        }
        int count = 0;
        for (SongVerse verse : mainSong.getVerses()) {
            if (verse.getSectionType() == sectionType) {
                ++count;
                if (verse.equals(this)) {
                    break;
                }
            }
        }
        return count;
    }

    public int getSongVerseCountBySectionType() {
        return getSongVerseCountBySectionType(getSectionType());
    }

    private boolean hasOtherSameTypeInSong() {
        if (mainSong == null) {
            return false;
        }
        for (SongVerse verse : mainSong.getVerses()) {
            if (verse.getSectionType() == getSectionType() && !verse.equals(this)) {
                return true;
            }
        }
        return false;
    }

    public String getSectionTypeStringWithCount() {
        String sectionTypeString = getSectionTypeString();
        if (hasOtherSameTypeInSong()) {
            sectionTypeString += getSongVerseCountBySectionType();
        }
        return sectionTypeString;
    }

    public String getSectionTypeString(SectionType sectionType) {
        Settings settings = Settings.getInstance();
        ResourceBundle bundle = settings.getResourceBundle();
        switch (sectionType) {
            case INTRO:
                return bundle.getString("intro");
            case VERSE:
                return bundle.getString("verse");
            case PRE_CHORUS:
                return bundle.getString("pre_chorus");
            case CHORUS:
                return bundle.getString("chorus");
            case BRIDGE:
                return bundle.getString("bridge");
            case CODA:
                return bundle.getString("coda");
        }
        return "";
    }

    public boolean equals(SongVerse other) {
        String uuid = getUuid();
        String otherUuid = other.getUuid();
        if (uuid != null && otherUuid != null) {
            return uuid.equals(otherUuid);
        }
        Long id = getId();
        Long otherId = other.getId();
        if (id != null && otherId != null) {
            return id.equals(otherId);
        }
        return text.equals(other.text);
    }
}
