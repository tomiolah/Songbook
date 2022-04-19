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

    public int getOrdinalNumberInt() {
        try {
            return Integer.parseInt(ordinalNumber.replaceAll("[^0-9]*", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    public String getOrdinalNumber() {
        return ordinalNumber.replaceAll("^0+.$", "");
    }

    public void setOrdinalNumber(String ordinalNumber) {
        if (ordinalNumber == null) {
            this.ordinalNumber = null;
        } else {
            this.ordinalNumber = ordinalNumber.replaceAll("^0+.$", "");
        }
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
