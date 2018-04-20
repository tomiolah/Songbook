package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.LanguageRepository;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.ServiceException;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class SongServiceImpl extends BaseServiceImpl<Song> implements SongService {
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private LanguageRepository languageRepository;

    @Override
    public List<Song> findAllAfterModifiedDate(Date lastModifiedDate) {
        final List<Song> songs = new ArrayList<>();
        List<Song> allByModifiedDateGreaterThan = songRepository.findAllByModifiedDateGreaterThan(lastModifiedDate);
        addAfterModifiedDateSongs(lastModifiedDate, allByModifiedDateGreaterThan, songs);
        return songs;
    }

    private void addAfterModifiedDateSongs(Date lastModifiedDate, List<Song> songs, List<Song> all) {
        for (Song song : songs) {
            if (!song.isDeleted() || song.getCreatedDate().compareTo(lastModifiedDate) <= 0) {
                all.add(song);
            }
        }
    }

    private void addSongs(List<Song> songs, List<Song> all) {
        for (Song song : songs) {
            if (!song.isDeleted()) {
                all.add(song);
            }
        }
    }

    @Override
    public List<Song> findAll() {
        final List<Song> songs = new ArrayList<>();
        List<Language> languages = languageRepository.findAll();
        for (Language language : languages) {
            language.setLanguageForSongs();
            addSongs(language.getSongs(), songs);
        }
        return songs;
    }

    @Override
    public List<Song> findAllByLanguage(String languageId) {
        final List<Song> songs = new ArrayList<>();
        Language language = languageRepository.findOne(languageId);
        addSongs(language.getSongs(), songs);
        return songs;
    }

    @Override
    public List<Song> findAllByLanguageAndModifiedDate(String languageId, Date lastModifiedDate) {
        List<Song> returningSongs = new ArrayList<>();
        Language language = languageRepository.findOne(languageId);
        addSongs(language.getSongs(), lastModifiedDate, returningSongs);
        return returningSongs;
    }

    @Override
    public List<Song> findAllByUploadedTrueAndDeletedTrue() {
        return songRepository.findAllByUploadedTrueAndDeletedTrue();
    }

    @Override
    public List<Song> findAllSimilar(Song song) {
        List<Song> all = songRepository.findAll();
        List<Song> similar = new ArrayList<>();
        String text = getText(song);
        String songId = song.getId();
        String regex = "[.,;?_\"'\\n!:/|\\\\ ]";
        String[] split = text.split(regex);
        int wordsLength = split.length;
        HashMap<String, Boolean> wordHashMap = new HashMap<>(wordsLength);
        for (String word : split) {
            wordHashMap.put(word.toLowerCase(), true);
        }
        int size = wordHashMap.keySet().size();
        HashMap<String, Boolean> hashMap = new HashMap<>(size);
        for (Song databaseSong : all) {
            if ((songId != null && databaseSong.getId().equals(songId)) || databaseSong.isDeleted()) {
                continue;
            }
            String secondText = getText(databaseSong);
            String[] words = secondText.split(regex);
            hashMap.clear();
            int count = 0;
            for (String word : words) {
                hashMap.put(word.toLowerCase(), true);
            }
            for (String word : hashMap.keySet()) {
                if (wordHashMap.containsKey(word)) {
                    ++count;
                }
            }
            double x = count;
            x /= size;
            if (x > 0.5) {
                int highestCommonStringInt = StringUtils.highestCommonStringInt(text, secondText);
                x = highestCommonStringInt;
                x = x / text.length();
                if (x > 0.55) {
                    double y;
                    y = highestCommonStringInt;
                    y = y / secondText.length();
                    if (y > 0.55) {
                        int i = 0;
                        x = (x + y) / 2;
                        databaseSong.setPercentage(x);
                        for (; i < similar.size(); ++i) {
                            if (similar.get(i).getPercentage() < x) {
                                break;
                            }
                        }
                        similar.add(i, databaseSong);
                    }
                }
            }
        }
        return similar;
    }

    @Override
    public boolean matches(Song song, Song song2) {
        if (!song.getTitle().equals(song2.getTitle())) {
            return false;
        }
        List<SongVerse> songVerses = song.getVerses();
        List<SongVerse> song2Verses = song2.getVerses();
        if (songVerses.size() != song2Verses.size()) {
            return false;
        }
        for (int i = 0; i < songVerses.size(); ++i) {
            if (!songVerses.get(i).matches(song2Verses.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        List<Song> allByVersionGroup = songRepository.findAllByVersionGroup(versionGroup);
        Song one = songRepository.findOne(versionGroup);
        if (one != null) {
            String group = one.getVersionGroup();
            if (group == null || !group.equals(versionGroup)) {
                allByVersionGroup.add(one);
            }
        }
        return allByVersionGroup;
    }

    @SuppressWarnings("Duplicates")
    private String getText(Song song) {
        ArrayList<SongVerse> verseList = new ArrayList<>(song.getVerses().size());
        final List<SongVerse> verses = song.getVerses();
        SongVerse chorus = null;
        int size = verses.size();
        for (int i = 0; i < size; ++i) {
            SongVerse songVerse = verses.get(i);
            verseList.add(songVerse);
            if (songVerse.isChorus()) {
                chorus = songVerse;
            } else if (chorus != null) {
                if (i + 1 < size) {
                    if (!verses.get(i + 1).isChorus()) {
                        verseList.add(chorus);
                    }
                } else {
                    verseList.add(chorus);
                }
            }
        }
        StringBuilder text = new StringBuilder();
        for (SongVerse songVerse : verseList) {
            text.append(songVerse.getText()).append(" ");
        }
        return text.toString();
    }

    private void addSongs(List<Song> songs, Date lastModifiedDate, List<Song> returningSongs) {
        for (Song song : songs) {
            if ((!song.isDeleted() || song.getCreatedDate().compareTo(lastModifiedDate) < 0) && song.getModifiedDate().getTime() > lastModifiedDate.getTime()) {
                returningSongs.add(song);
            }
        }
    }

    @Override
    public Song findOne(String id) {
        Song song = super.findOne(id);
        if (song != null) {
            Language languageBySongsContaining = languageRepository.findLanguageBySongsContaining(song);
            song.setLanguage(languageBySongsContaining);
        }
        return song;
    }

    @Override
    public Song save(Song song) {
        if (song.getTitle() == null || song.getTitle().trim().isEmpty()) {
            throw new ServiceException("No title", HttpStatus.PRECONDITION_FAILED);
        }
        if (song.isDeleted() && song.getLanguage() == null) {
            return songRepository.save(song);
        }
        if (song.getLanguage() == null) {
            throw new ServiceException("No language", HttpStatus.PRECONDITION_FAILED);
        }
        Language language = languageRepository.findOne(song.getLanguage().getId());
        if (song.getId() != null) {
            Song oneWithLanguage = findOne(song.getId());
            Language oldLanguage = oneWithLanguage.getLanguage();
            if (oldLanguage != null && !oldLanguage.equals(language)) {
                Song songToRemove = null;
                for (Song song1 : oldLanguage.getSongs()) {
                    if (song1.getId().equals(song.getId())) {
                        songToRemove = song1;
                        break;
                    }
                }
                oldLanguage.getSongs().remove(songToRemove);
                languageRepository.save(oldLanguage);
            }
        }
        songRepository.save(song);
        if (languageRepository.findLanguageBySongsContaining(song) == null) {
            boolean was = false;
            for (Song song1 : language.getSongs()) {
                if (song1.getId().equals(song.getId())) {
                    was = true;
                }
            }
            if (!was) {
                language.getSongs().add(song);
                languageRepository.save(language);
            }
        }
        return song;
    }

    @Override
    public Iterable save(List<Song> songs) {
        for (Song song : songs) {
            save(song);
        }
        return songs;
    }
}
