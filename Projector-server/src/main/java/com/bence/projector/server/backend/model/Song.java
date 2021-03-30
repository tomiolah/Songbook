package com.bence.projector.server.backend.model;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Song extends BaseEntity {

    private String originalId;
    private String title;
    private List<SongVerse> verses;
    private Date createdDate;
    private Date modifiedDate;
    private boolean deleted = false;
    @Transient
    private Language language;
    private Boolean uploaded;
    private long views;
    private Date lastIncrementViewDate;
    private long favourites;
    private Date lastIncrementFavouritesDate;
    private String createdByEmail;
    @Transient
    transient private double percentage;
    private String versionGroup;
    private String youtubeUrl;
    private String verseOrder;
    private String author;
    private List<Short> verseOrderList;
    @DBRef
    private User lastModifiedBy;
    @DBRef
    private Song backUp;
    private Boolean isBackUp;
    private Boolean reviewerErased;
    @Transient
    private List<Language> previousLanguages;
    @Transient
    private String beforeId;

    public Song() {
    }

    public Song(Song song) {
        originalId = song.originalId;
        title = song.title;
        verses = song.verses;
        createdDate = song.createdDate;
        modifiedDate = song.modifiedDate;
        language = song.language;
        uploaded = song.uploaded;
        views = song.views;
        lastIncrementViewDate = song.lastIncrementViewDate;
        favourites = song.favourites;
        lastIncrementFavouritesDate = song.lastIncrementFavouritesDate;
        createdByEmail = song.createdByEmail;
        percentage = song.percentage;
        versionGroup = song.versionGroup;
        youtubeUrl = song.youtubeUrl;
        verseOrder = song.verseOrder;
        author = song.author;
        verseOrderList = song.verseOrderList;
        lastModifiedBy = song.lastModifiedBy;
        backUp = song.backUp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SongVerse> getVerses() {
        return verses;
    }

    public void setVerses(List<SongVerse> verses) {
        this.verses = verses;
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
    }

    public boolean isDeleted() {
        return deleted || isBackUp() || isReviewerErased();
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        if (this.language != null && (language == null || !this.language.getId().equals(language.getId()))) {
            getPreviousLanguages().add(this.language);
        }
        this.language = language;
    }

    @Override
    public String toString() {
        return title;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void incrementViews() {
        ++this.views;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public Date getLastIncrementViewDate() {
        return lastIncrementViewDate;
    }

    public void setLastIncrementViewDate(Date lastIncrementViewDate) {
        this.lastIncrementViewDate = lastIncrementViewDate;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public boolean isUploaded() {
        return uploaded != null && uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public Date getLastIncrementFavouritesDate() {
        return lastIncrementFavouritesDate;
    }

    public void setLastIncrementFavouritesDate(Date lastIncrementFavouritesDate) {
        this.lastIncrementFavouritesDate = lastIncrementFavouritesDate;
    }

    public void incrementFavourites() {
        ++favourites;
    }

    public long getFavourites() {
        return favourites;
    }

    public void setFavourites(long favourites) {
        this.favourites = favourites;
    }

    public String getVerseOrder() {
        return verseOrder;
    }

    public void setVerseOrder(String verseOrder) {
        this.verseOrder = verseOrder;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Short> getVerseOrderList() {
        if (verses != null && verseOrderList != null) {
            for (short index = 0; index < verses.size(); ++index) {
                if (!verseOrderList.contains(index)) {
                    verseOrderList.add(index);
                }
            }
        }
        return verseOrderList;
    }

    public void setVerseOrderList(List<Short> verseOrderList) {
        this.verseOrderList = verseOrderList;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Song getBackUp() {
        return backUp;
    }

    public void setIsBackUp(Boolean isBackUp) {
        this.isBackUp = isBackUp;
    }

    public boolean isBackUp() {
        return isBackUp != null && isBackUp;
    }

    public void setBackUp(Song backUp) {
        this.backUp = backUp;
    }

    public boolean isReviewerErased() {
        return reviewerErased != null && reviewerErased;
    }

    public void setReviewerErased(Boolean reviewerErased) {
        this.reviewerErased = reviewerErased;
    }

    public List<Language> getPreviousLanguages() {
        if (previousLanguages == null) {
            previousLanguages = new ArrayList<>();
        }
        return previousLanguages;
    }

    public String getBeforeId() {
        return beforeId;
    }

    public void setBeforeId(String beforeId) {
        this.beforeId = beforeId;
    }

    public boolean isPublic() {
        return !isReviewerErased() && !isDeleted() && !isBackUp();
    }

    private String idOrVersionGroup() {
        String versionGroup = getVersionGroup();
        if (versionGroup != null) {
            return versionGroup;
        }
        return getId();
    }

    public boolean isSameVersionGroup(Song other) {
        if (other == null) {
            return false;
        }
        return idOrVersionGroup().equals(other.idOrVersionGroup());
    }
}
