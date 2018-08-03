package projector.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.List;

import static projector.utils.StringUtils.stripAccents;

public class BibleVerse extends BaseEntity {

    @ForeignCollectionField
    private ForeignCollection<VerseIndex> verseIndexForeignCollection;
    @DatabaseField(width = 1000)
    private String text;
    @DatabaseField(width = 1000)
    private String strippedText;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private Chapter chapter;
    private List<VerseIndex> verseIndices;
    @DatabaseField
    private short number;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        strippedText = stripAccents(text.toLowerCase());
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public String getStrippedText() {
        return strippedText;
    }

    public void setStrippedText(String strippedText) {
        this.strippedText = strippedText;
    }

    @Override
    public String toString() {
        return text;
    }

    public List<VerseIndex> getVerseIndices() {
        if (verseIndices == null && verseIndexForeignCollection != null) {
            verseIndices = new ArrayList<>(verseIndexForeignCollection.size());
            verseIndices.addAll(verseIndexForeignCollection);
        }
        return verseIndices;
    }

    public void setVerseIndices(List<VerseIndex> verseIndices) {
        for (VerseIndex verseIndex : verseIndices) {
            verseIndex.setBibleVerse(this);
        }
        this.verseIndices = verseIndices;
    }

    public short getNumber() {
        return number;
    }

    public void setNumber(short number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof BibleVerse)) {
            return false;
        }
        BibleVerse bibleVerse = (BibleVerse) obj;
        if (getId() != null && bibleVerse.getId() != null && getId().equals(bibleVerse.getId())) {
            return true;
        }
        if (getUuid() != null && bibleVerse.getUuid() != null && getUuid().equals(bibleVerse.getUuid())) {
            return true;
        }
        return super.equals(obj);
    }
}
