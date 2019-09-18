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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.bence.projector.server.utils.StringUtils.longestCommonSubString;

@Service
public class SongServiceImpl extends BaseServiceImpl<Song> implements SongService {
    private final SongRepository songRepository;
    private final LanguageRepository languageRepository;
    private final String wordsSplit = "[.,;?_\"'\\n!:/|\\\\ ]";
    private HashMap<String, Song> songsHashMap;
    private long lastModifiedDateTime = 0;
    private HashMap<String, HashMap<String, Boolean>> wordsHashMapByLanguage;

    @Autowired
    public SongServiceImpl(SongRepository songRepository, LanguageRepository languageRepository) {
        this.songRepository = songRepository;
        this.languageRepository = languageRepository;
    }

    @Override
    public boolean isLanguageIsGood(Song song, Language language) {
        double x = getLanguagePercentage(song, language);
        return x > 0.7;
    }

    private double getLanguagePercentage(Song song, Language language) {
        HashMap<String, Boolean> wordsHashMap = getWordsHashMap(language);
        String text = getText(song);
        String[] split = text.split(wordsSplit);
        int count = 0;
        for (String s : split) {
            if (wordsHashMap.containsKey(s)) {
                ++count;
            }
        }
        int totalWordCount = split.length;
        double x = count;
        x /= totalWordCount;
        return x;
    }

    @Override
    public Language bestLanguage(Song song, List<Language> languages) {
        List<Language> localLanguages = new ArrayList<>(languages.size());
        for (Language language : languages) {
            language.setPercentage(getLanguagePercentage(song, language));
            localLanguages.add(language);
        }
        localLanguages.sort((o1, o2) -> Double.compare(o2.getPercentage(), o1.getPercentage()));
        return localLanguages.get(0);
    }

    private HashMap<String, Boolean> getWordsHashMap(Language language) {
        HashMap<String, HashMap<String, Boolean>> wordsHashMapByLanguage = getWordsHashMapByLanguage();
        HashMap<String, Boolean> wordsHashMap = wordsHashMapByLanguage.get(language.getId());
        if (wordsHashMap == null) {
            wordsHashMap = new HashMap<>();
            List<Song> allByLanguage = findAllByLanguage(language.getId());
            for (Song song : allByLanguage) {
                String text = getText(song);
                String[] split = text.split(wordsSplit);
                for (String s : split) {
                    wordsHashMap.put(s, true);
                }
            }
            wordsHashMapByLanguage.put(language.getId(), wordsHashMap);
        }
        return wordsHashMap;
    }

    @Override
    public List<Song> findAllAfterModifiedDate(Date lastModifiedDate) {
        final List<Song> songs = new ArrayList<>();
        List<Song> allByModifiedDateGreaterThan;
        if (lastModifiedDate.getTime() < 1000) {
            allByModifiedDateGreaterThan = findAllSongsLazy();
        } else {
            allByModifiedDateGreaterThan = songRepository.findAllByModifiedDateGreaterThan(lastModifiedDate);
        }
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
        List<Song> allByUploadedTrueAndDeletedTrue = new LinkedList<>();
        for (Song song : getSongs()) {
            if (song.isUploaded() && song.isDeleted()) {
                allByUploadedTrueAndDeletedTrue.add(song);
            }
        }
        return allByUploadedTrueAndDeletedTrue;
    }

    @Override
    public List<Song> findAllSimilar(Song song) {
        return findAllSimilar(song, false);
    }

    @Override
    public void delete(String id) {
        super.delete(id);
        if (songsHashMap != null) {
            songsHashMap.remove(id);
        }
    }

    @Override
    public void delete(List<String> ids) {
        for (String id : ids) {
            delete(id);
        }
    }

    @Override
    public List<Song> findAllSimilar(Song song, boolean checkDeleted) {
        Collection<Song> all = getSongs();
        List<Song> similar = new ArrayList<>();
        String text = getText(song);
        String songId = song.getId();
        String regex = wordsSplit;
        String[] split = text.split(regex);
        int wordsLength = split.length;
        HashMap<String, Boolean> wordHashMap = new HashMap<>(wordsLength);
        for (String word : split) {
            wordHashMap.put(word.toLowerCase(), true);
        }
        int size = wordHashMap.keySet().size();
        HashMap<String, Boolean> hashMap = new HashMap<>(size);
        for (Song databaseSong : all) {
            //noinspection PointlessNullCheck
            if ((songId != null && databaseSong.getId().equals(songId)) || (databaseSong.isDeleted() && !checkDeleted)) {
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
                boolean wasSimilar = false;
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
                        wasSimilar = true;
                    }
                }
                if (!wasSimilar) {
                    if (longestCommonSubString(text, secondText) > 50) {
                        databaseSong.setPercentage(0);
                        similar.add(databaseSong);
                    }
                }
            }
        }
        return similar;
    }

    @Override
    public void enrollSongInMap(Song song) {
        Language language = song.getLanguage();
        if (language == null) {
            return;
        }
        HashMap<String, Boolean> wordsHashMap = getWordsHashMap(language);
        String text = getText(song);
        String[] split = text.split(wordsSplit);
        for (String s : split) {
            wordsHashMap.put(s, true);
        }
    }

    private Collection<Song> getSongs() {
        if (songsHashMap == null) {
            songsHashMap = new HashMap<>(16221);
            for (Song song : songRepository.findAll()) {
                putInMap(song);
            }
        } else {
            for (Song song : songRepository.findAllByModifiedDateGreaterThan(new Date(lastModifiedDateTime))) {
                if (!songsHashMap.containsKey(song.getId())) {
                    putInMap(song);
                } else {
                    songsHashMap.replace(song.getId(), song);
                    checkLastModifiedDate(song);
                }
            }
        }
        return songsHashMap.values();
    }

    private void checkLastModifiedDate(Song song) {
        long time = song.getModifiedDate().getTime();
        if (time > lastModifiedDateTime) {
            lastModifiedDateTime = time;
        }
    }

    private void putInMap(Song song) {
        songsHashMap.put(song.getId(), song);
        checkLastModifiedDate(song);
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
        ArrayList<Song> songs = new ArrayList<>();
        for (Song song : allByVersionGroup) {
            if (!song.isDeleted()) {
                songs.add(song);
            }
        }
        Song one = songRepository.findOne(versionGroup);
        if (one != null && !one.isDeleted()) {
            String group = one.getVersionGroup();
            if (group == null || !group.equals(versionGroup)) {
                songs.add(one);
            }
        }
        return songs;
    }

    @Override
    public Song getRandomSong() {
        Random random = new Random();
        Collection<Song> songs = getSongs();
        int n = random.nextInt(songs.size());
        Iterator<Song> iterator = songs.iterator();
        for (int i = 0; i < n && iterator.hasNext(); ++i) {
            iterator.next();
        }
        if (!iterator.hasNext()) {
            return songs.iterator().next();
        }
        Song song = iterator.next();
        while (song.isDeleted() && iterator.hasNext()) {
            song = iterator.next();
        }
        return song;
    }

    @SuppressWarnings("Duplicates")
    private String getText(Song song) {
        ArrayList<SongVerse> verseList = new ArrayList<>(song.getVerses().size());
        final List<SongVerse> verses = song.getVerses();
        int size = verses.size();
        List<Short> verseOrderList = song.getVerseOrderList();
        if (verseOrderList == null) {
            SongVerse chorus = null;
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
        } else {
            for (Short i : verseOrderList) {
                if (i < verses.size()) {
                    verseList.add(verses.get(i));
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
            try {
                if ((!song.isDeleted() || song.getCreatedDate().compareTo(lastModifiedDate) < 0) && song.getModifiedDate().getTime() > lastModifiedDate.getTime()) {
                    returningSongs.add(song);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
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
                removeSongFromLanguage(song, oldLanguage);
            }
        }
        songRepository.save(song);
        if (languageRepository.findLanguageBySongsContaining(song) == null) {
            boolean was = false;
            for (Song song1 : language.getSongs()) {
                if (song1.getId().equals(song.getId())) {
                    was = true;
                    break;
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
    public void removeSongFromLanguage(Song song, Language oldLanguage) {
        Song songToRemove = null;
        for (Song song1 : oldLanguage.getSongs()) {
            if (song1 != null && song1.getId().equals(song.getId())) {
                songToRemove = song1;
                break;
            }
        }
        oldLanguage.getSongs().remove(songToRemove);
        languageRepository.save(oldLanguage);
    }

    @Override
    public List<Song> findAllContainingYoutubeUrl() {
        return songRepository.findAllByYoutubeUrlNotNull();
    }

    @Override
    public List<Song> findAllByLanguageContainingViews(String languageId) {
        Language language = languageRepository.findOne(languageId);
        List<Song> songs = new ArrayList<>(language.getSongs().size());
        for (Song song : language.getSongs()) {
            if (song.isDeleted() || song.getViews() == 0) {
                continue;
            }
            songs.add(song);
        }
        return songs;
    }

    @Override
    public List<Song> findAllByLanguageContainingFavourites(String languageId) {
        Language language = languageRepository.findOne(languageId);
        List<Song> songs = new ArrayList<>(language.getSongs().size());
        for (Song song : language.getSongs()) {
            if (song.isDeleted() || song.getFavourites() == 0) {
                continue;
            }
            songs.add(song);
        }
        return songs;
    }

    @Override
    public List<Song> findAllSongsLazy() {
        Collection<Song> songs = getSongs();
        List<Song> songList = new ArrayList<>(songs.size());
        songList.addAll(songs);
        return songList;
    }

    @Override
    public Iterable save(List<Song> songs) {
        for (Song song : songs) {
            save(song);
        }
        return songs;
    }

    private HashMap<String, HashMap<String, Boolean>> getWordsHashMapByLanguage() {
        if (wordsHashMapByLanguage == null) {
            wordsHashMapByLanguage = new HashMap<>();
        }
        return wordsHashMapByLanguage;
    }
}
