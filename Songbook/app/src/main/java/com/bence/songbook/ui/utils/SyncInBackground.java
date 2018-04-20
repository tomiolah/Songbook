package com.bence.songbook.ui.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.api.SongCollectionApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.SongCollectionRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SyncInBackground {

    private static SyncInBackground instance;
    private Long syncFrom;

    private SyncInBackground() {
    }

    public static SyncInBackground getInstance() {
        if (instance == null) {
            instance = new SyncInBackground();
        }
        return instance;
    }

    public void sync(Context context) {
        LanguageRepository languageRepository = new LanguageRepositoryImpl(context);
        List<Language> languages = languageRepository.findAll();
        for (Language language : languages) {
            new Downloader(language, context).execute();
        }
    }

    private void sortSongs(List<Song> all) {
        Collections.sort(all, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return rhs.getModifiedDate().compareTo(lhs.getModifiedDate());
            }
        });
    }

    public void setSyncFrom() {
        this.syncFrom = 1524234911591L;
    }

    @SuppressLint("StaticFieldLeak")
    class Downloader extends AsyncTask<Void, Integer, Void> {
        private final Context context;
        private Language language;
        private List<SongCollection> onlineModifiedSongCollections;

        Downloader(Language language, Context context) {
            this.language = language;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SongRepository songRepository = new SongRepositoryImpl(context);
            LanguageRepository languageRepository = new LanguageRepositoryImpl(context);
            final SongApiBean songApiBean = new SongApiBean();
            List<Song> languageSongs = language.getSongs();
            sortSongs(languageSongs);
            Long modifiedDate;
            if (languageSongs.size() > 0) {
                modifiedDate = languageSongs.get(0).getModifiedDate().getTime();
            } else {
                modifiedDate = 0L;
            }
            if (syncFrom != null) {
                modifiedDate = syncFrom;
            }
            List<Song> onlineModifiedSongs = songApiBean.getSongsByLanguageAndAfterModifiedDate(language, modifiedDate);
            if (onlineModifiedSongs != null) {
                saveSongs(songRepository, languageRepository, languageSongs, onlineModifiedSongs);
            }

            SongCollectionApiBean songCollectionApiBean = new SongCollectionApiBean();
            SongCollectionRepository songCollectionRepository = new SongCollectionRepositoryImpl(context);
            List<SongCollection> songCollectionRepositoryAll = songCollectionRepository.findAllByLanguage(language);
            Date lastModifiedDate = new Date(0);
            for (SongCollection songCollection : songCollectionRepositoryAll) {
                Date songCollectionModifiedDate = songCollection.getModifiedDate();
                if (songCollectionModifiedDate.compareTo(lastModifiedDate) > 0) {
                    lastModifiedDate = songCollectionModifiedDate;
                }
            }
            onlineModifiedSongCollections = songCollectionApiBean.getSongCollections(language, lastModifiedDate);
            if (onlineModifiedSongCollections != null) {
                saveSongCollections(songCollectionRepository, songCollectionRepositoryAll, languageRepository);
            }
            return null;
        }

        private void saveSongs(SongRepository songRepository, LanguageRepository languageRepository, List<Song> languageSongs, List<Song> onlineModifiedSongs) {
            HashMap<String, Song> songHashMap = new HashMap<>(languageSongs.size());
            for (Song song : languageSongs) {
                songHashMap.put(song.getUuid(), song);
            }
            List<Song> needToRemove = new ArrayList<>();
            for (Song song : onlineModifiedSongs) {
                if (songHashMap.containsKey(song.getUuid())) {
                    Song modifiedSong = songHashMap.get(song.getUuid());
                    needToRemove.add(modifiedSong);
                    languageSongs.remove(modifiedSong);
                }
            }
            sortSongs(onlineModifiedSongs);
            languageSongs.addAll(onlineModifiedSongs);
            language.setSongs(languageSongs);
            songRepository.deleteAll(needToRemove);
            songRepository.save(onlineModifiedSongs);
            languageRepository.save(language);
        }

        private void saveSongCollections(SongCollectionRepository songCollectionRepository, List<SongCollection> songCollectionRepositoryAll, LanguageRepository languageRepository) {
            HashMap<String, SongCollection> songCollectionHashMap = new HashMap<>(songCollectionRepositoryAll.size());
            for (SongCollection songCollection : songCollectionRepositoryAll) {
                songCollectionHashMap.put(songCollection.getUuid(), songCollection);
            }
            List<SongCollection> needToDelete = new ArrayList<>();
            for (SongCollection songCollection : onlineModifiedSongCollections) {
                if (songCollectionHashMap.containsKey(songCollection.getUuid())) {
                    SongCollection modifiedSongCollection = songCollectionHashMap.get(songCollection.getUuid());
                    needToDelete.add(modifiedSongCollection);
                }
            }
            songCollectionRepository.deleteAll(needToDelete);
            songCollectionRepository.save(onlineModifiedSongCollections);
            languageRepository.save(language);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
