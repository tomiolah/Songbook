package projector.model;

import com.j256.ormlite.field.DatabaseField;

public class SongCollectionElement extends BaseEntity {

    @DatabaseField
    private String ordinalNumber;
    @DatabaseField
    private String songUuid;
    @DatabaseField(foreign = true, index = true)
    private SongCollection songCollection;

    public String getOrdinalNumber() {
        return ordinalNumber;
    }

    public void setOrdinalNumber(String ordinalNumber) {
        this.ordinalNumber = ordinalNumber;
    }

    @Override
    public String toString() {
        return ordinalNumber;
    }

    public String getSongUuid() {
        return songUuid;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }

    public SongCollection getSongCollection() {
        return songCollection;
    }

    public void setSongCollection(SongCollection songCollection) {
        this.songCollection = songCollection;
    }
}
