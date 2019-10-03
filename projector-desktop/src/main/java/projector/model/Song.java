package projector.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static projector.utils.StringUtils.stripAccents;

public class Song extends BaseEntity {

    private static long currentDate = new Date().getTime();
    @Expose
    @DatabaseField
    private String title;
    @DatabaseField
    private String strippedTitle;
    @ForeignCollectionField
    private ForeignCollection<SongVerse> songVerseForeignCollection;
    @Expose
    private List<SongVerse> verses;
    @Expose
    @DatabaseField
    private Date createdDate;
    @Expose
    @DatabaseField
    private Date modifiedDate;
    @DatabaseField
    private Date serverModifiedDate;
    private transient boolean deleted;
    //	@Transient
    private String fileText;
    //	@Transient
    private double[] versTimes;
    //	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "songs")
    private List<SongBook> songBooks;
    @Expose
    @DatabaseField
    private boolean publish = true;
    @Expose
    @DatabaseField
    private boolean published = false;
    @DatabaseField(foreign = true, index = true)
    private Language language;
    private transient SongCollection songCollection;
    private transient SongCollectionElement songCollectionElement;
    @Expose
    @DatabaseField
    private String versionGroup;
    @DatabaseField
    private long views;
    @DatabaseField
    private long favouriteCount;
    @DatabaseField
    private String author;

    public Song() {
    }

    public Song(String title, List<SongVerse> verses, String fileText, double[] versTimes, List<SongBook> songBooks) {
        this.title = title;
        this.verses = SongVerse.cloneList(verses);
        this.fileText = fileText;
        this.versTimes = versTimes == null ? null : versTimes.clone();
        this.songBooks = songBooks;
    }

    private static long getCurrentDate() {
        return currentDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        strippedTitle = stripAccents(title.toLowerCase());
    }

    public String getFileText() {
        return fileText;
    }

    public void setFileText(String fileText) {
        this.fileText = fileText;
    }

    public double[] getVersTimes() {
        return versTimes == null ? null : versTimes.clone();
    }

    public void setVersTimes(double[] versTimes) {
        this.versTimes = versTimes.clone();
    }

    public List<SongBook> getSongBooks() {
        return songBooks;
    }

    public void setSongBooks(List<SongBook> songBooks) {
        this.songBooks = songBooks;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : (Date) createdDate.clone();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate == null ? null : (Date) createdDate.clone();
    }

    public Date getModifiedDate() {
        return modifiedDate == null ? new Date(0) : (Date) modifiedDate.clone();
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public List<SongVerse> getVerses() {
        if (verses == null) {
            List<SongVerse> songVerses = new ArrayList<>(songVerseForeignCollection.size());
            songVerses.addAll(songVerseForeignCollection);
            verses = songVerses;
            return songVerses;
        }
        return verses;
    }

    public void setVerses(List<SongVerse> verseList) {
        for (SongVerse songVerse : verseList) {
            songVerse.setMainSong(this);
        }
        this.verses = verseList;
    }

    public void fetchVerses() {
        if (verses == null) {
            List<SongVerse> songVerses = new ArrayList<>(songVerseForeignCollection.size());
            songVerses.addAll(songVerseForeignCollection);
            verses = songVerses;
        }
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getStrippedTitle() {
        return strippedTitle;
    }

    public String getVersesText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SongVerse songVerse : getVerses()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(songVerse.getText());
        }
        return stringBuilder.toString();
    }

    public SongCollection getSongCollection() {
        return songCollection;
    }

    public void setSongCollection(SongCollection songCollection) {
        this.songCollection = songCollection;
    }

    public SongCollectionElement getSongCollectionElement() {
        return songCollectionElement;
    }

    public void setSongCollectionElement(SongCollectionElement songCollectionElement) {
        this.songCollectionElement = songCollectionElement;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getServerModifiedDate() {
        return serverModifiedDate;
    }

    public void setServerModifiedDate(Date serverModifiedDate) {
        this.serverModifiedDate = serverModifiedDate;
    }

    @Override
    public String toString() {
        String text = this.title;
        if (songCollection != null) {
            text += " " + songCollection.getName();
            if (songCollectionElement != null) {
                text += " " + songCollectionElement.getOrdinalNumber();
            }
        }
        return text;
    }

    public void stripTitle() {
        strippedTitle = stripAccents(title.toLowerCase());
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    private long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    private long getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(long favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public Integer getScore() {
        int score = 0;
        score += getViews();
        score += getFavouriteCount() * 2;
        if (createdDate == null || modifiedDate == null) {
            return score;
        }
        long l = Song.getCurrentDate() - createdDate.getTime();
        if (l < 2592000000L) {
            score += 14 * ((1 - (double) l / 2592000000L));
        }
        l = Song.getCurrentDate() - modifiedDate.getTime();
        if (l < 2592000000L) {
            score += 4 * ((1 - (double) l / 2592000000L));
        }
        return score;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
