package projector.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

public class SongCollectionElement extends BaseEntity {

    @Expose
    @DatabaseField
    private String ordinalNumber;
    @Expose
    @DatabaseField
    private String songUuid;
    @DatabaseField(foreign = true, index = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
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
