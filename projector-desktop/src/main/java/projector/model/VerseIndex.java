package projector.model;

import com.j256.ormlite.field.DatabaseField;

public class VerseIndex extends BaseEntity {
    @DatabaseField
    private Long indexNumber;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private BibleVerse bibleVerse;

    public BibleVerse getBibleVerse() {
        return bibleVerse;
    }

    void setBibleVerse(BibleVerse bibleVerse) {
        this.bibleVerse = bibleVerse;
    }

    public Long getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(Long indexNumber) {
        this.indexNumber = indexNumber;
    }
}
