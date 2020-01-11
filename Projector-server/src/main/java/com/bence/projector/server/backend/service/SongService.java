package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;

import java.util.Date;
import java.util.List;

public interface SongService extends BaseService<Song> {
    List<Song> findAllAfterModifiedDate(Date lastModifiedDate);

    List<Song> findAllByLanguage(String languageId);

    List<Song> findAllByLanguageAndModifiedDate(String languageId, Date date);

    List<Song> findAllByUploadedTrueAndDeletedTrue();

    List<Song> findAllSimilar(Song song);

    boolean matches(Song song, Song song2);

    List<Song> findAllByVersionGroup(String versionGroup);

    Song getRandomSong();

    void removeSongFromLanguage(Song song, Language oldLanguage);

    List<Song> findAllContainingYoutubeUrl();

    List<Song> findAllByLanguageContainingViews(String languageId);

    List<Song> findAllByLanguageContainingFavourites(String languageId);

    List<Song> findAllSongsLazy();

    void delete(String id);

    boolean isLanguageIsGood(Song song, Language language);

    Language bestLanguage(Song song, List<Language> languages);

    List<Song> findAllSimilar(Song song, boolean checkDeleted);

    void enrollSongInMap(Song song);

    List<Song> findAllInReviewByLanguage(Language language);
}
