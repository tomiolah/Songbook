package projector.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongCollection extends BaseEntity {
    @ForeignCollectionField
    private ForeignCollection<SongCollectionElement> songCollectionElementForeignCollection;
    @Expose
    private List<SongCollectionElement> songCollectionElements;
    @Expose
    @DatabaseField
    private Date createdDate;
    @Expose
    @DatabaseField
    private Date modifiedDate;
    @Expose
    @DatabaseField
    private String name;
    @Expose
    @DatabaseField(foreign = true, index = true)
    private Language language;
    private boolean selected;
    private List<Song> songs;

    public SongCollection() {
    }

    public SongCollection(String name) {
        this.name = name;
    }

    public List<SongCollectionElement> getSongCollectionElements() {
        if (songCollectionElements == null) {
            //noinspection ConstantConditions
            if (songCollectionElementForeignCollection == null) {
                songCollectionElements = new ArrayList<>();
                return songCollectionElements;
            }
            songCollectionElements = new ArrayList<>(songCollectionElementForeignCollection.size());
            songCollectionElements.addAll(songCollectionElementForeignCollection);
        }
        return songCollectionElements;
    }

    public void setSongCollectionElements(List<SongCollectionElement> songCollectionElements) {
        for (SongCollectionElement songCollectionElement : songCollectionElements) {
            songCollectionElement.setSongCollection(this);
        }
        this.songCollectionElements = songCollectionElements;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : (Date) createdDate.clone();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate == null ? null : (Date) createdDate.clone();
    }

    public Date getModifiedDate() {
        return modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate == null ? null : (Date) modifiedDate.clone();
        if (modifiedDate == null) {
            this.modifiedDate = new Date(0);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<Song> getSongs() {
        if (songs == null) {
            SongService songService = ServiceManager.getSongService();
            songs = new ArrayList<>();
            for (SongCollectionElement songCollectionElement : getSongCollectionElements()) {
                Song song = songService.findByUuid(songCollectionElement.getSongUuid());
                if (song != null) {
                    song.setSongCollection(this);
                    song.setSongCollectionElement(songCollectionElement);
                    songs.add(song);
                }
            }
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
