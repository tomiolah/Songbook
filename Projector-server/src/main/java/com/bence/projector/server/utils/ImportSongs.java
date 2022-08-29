package com.bence.projector.server.utils;

import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bence.projector.server.utils.StringUtils.formatSongs;

public class ImportSongs {

    public static List<Song> importJsonSongs(LanguageService languageService, SongService songService) {
        List<Song> songs = new ArrayList<>();
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream("injili.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            ArrayList<ISong> songArrayList;
            Type listType = new TypeToken<ArrayList<ISong>>() {
            }.getType();
            songArrayList = gson.fromJson(s.toString(), listType);
            Language swahiliLanguage = findSwahiliLanguage(languageService);
            prepareSongs(songs, songArrayList, swahiliLanguage);
            songService.save(songs);
        } catch (IOException ignored) {
        }
        return songs;
    }

    public static void markSimilarSongsAsDeleted(SongService songService, SongRepository songRepository) {
        List<Song> songs = songService.findAllSongsLazy();
        List<Song> deletedSongs = new ArrayList<>();
        for (Song song : songs) {
            if ("cfmstreaming@gmail.com".equals(song.getCreatedByEmail())) {
                List<Song> allSimilar = songService.findAllSimilar(song);
                if (allSimilar != null && allSimilar.size() > 0) {
                    song.setDeleted(true);
                    song.setModifiedDate(new Date());
                    deletedSongs.add(song);
                }
            }
        }
        songRepository.save(deletedSongs);
    }

    private static Language findSwahiliLanguage(LanguageService languageService) {
        return languageService.findOneByUuid("6af972e9-e9b9-48a9-aaa5-ecebfbd38a95");
    }

    private static void prepareSongs(List<Song> songs, ArrayList<ISong> songArrayList, Language swahiliLanguage) {
        for (ISong iSong : songArrayList) {
            Song song = new Song();
            song.setCreatedByEmail("cfmstreaming@gmail.com");
            song.setCreatedDate(new Date());
            song.setModifiedDate(song.getCreatedDate());
            song.setTitle(iSong.title);
            ArrayList<SongVerse> verses = new ArrayList<>();
            ArrayList<Short> verseOrderList = new ArrayList<>();
            Short lastChorusIndex = null;
            Short index = -1;
            for (IVerse iVerse : iSong.verses) {
                if (iVerse.text.isEmpty()) {
                    continue;
                }
                ++index;
                SongVerse verse = new SongVerse();
                verse.setText(iVerse.text);
                verses.add(verse);
                verseOrderList.add(index);
                if (iVerse.chorus) {
                    verse.setSectionType(SectionType.CHORUS);
                    lastChorusIndex = index;
                } else {
                    verse.setSectionType(SectionType.VERSE);
                    if (lastChorusIndex != null) {
                        verseOrderList.add(lastChorusIndex);
                    }
                }
            }
            song.setVerseOrderList(verseOrderList);
            song.setVerses(verses);
            song.setLanguage(swahiliLanguage);
            songs.add(song);
        }
        formatSongs(songs);
        corrigateTitles(songs);
        System.out.println(songs.size());
    }

    private static void corrigateTitles(List<Song> songs) {
        //        Map<String, Boolean> upperWords = gatherWordsWithStartUpper(songs);
        for (Song song : songs) {
            song.setTitle(upperTitleWithUpperWords(song.getTitle()));
        }
    }

    private static String capitalize(String s) {
        try {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println(s);
            e.printStackTrace();
            return s;
        }
    }

    private static String upperTitleWithUpperWords(String title) {
        List<String> splitWithDelimiters = splitWithDelimiters(title);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < splitWithDelimiters.size(); ++i) {
            String split = splitWithDelimiters.get(i);
            String s1 = split.toLowerCase();
            String s2 = capitalize(s1);
            if ((notInExceptions(s1) || lastWord(splitWithDelimiters, i)) && !contractionWithApostrophe(splitWithDelimiters, i)) {
                s.append(s2);
            } else {
                s.append(s1);
            }
        }
        return capitalize(s.toString());
    }

    private static boolean contractionWithApostrophe(List<String> splitWithDelimiters, int i) {
        if (i < 2) {
            return false;
        }
        return splitWithDelimiters.get(i - 1).equals("'") && splitWithDelimiters.get(i - 2).matches("[A-Za-z]*");
    }

    private static boolean notInExceptions(String s) {
        return !s.equals("a") && !s.equals("na") && !s.equals("ya") && !s.equals("au") && !s.equals("kwa") && !s.equals("juu");
    }

    private static boolean lastWord(List<String> splitWithDelimiters, int index) {
        for (int i = index + 1; i < splitWithDelimiters.size(); ++i) {
            String split = splitWithDelimiters.get(i);
            if (split.matches("^[A-Za-z]*$")) {
                return false;
            }
        }
        return true;
    }

    private static List<String> splitWithDelimiters(String s) {
        String[] split = s.split("\\W+");
        List<String> strings = new ArrayList<>();
        for (String aSplit : split) {
            int indexOf = s.indexOf(aSplit);
            String delimiter = s.substring(0, indexOf);
            s = s.substring(indexOf + aSplit.length());
            if (!delimiter.isEmpty()) {
                strings.add(delimiter);
            }
            if (!aSplit.isEmpty()) {
                strings.add(aSplit);
            }
        }
        if (!s.isEmpty()) {
            strings.add(s);
        }
        return strings;
    }

    private static Map<String, Boolean> gatherWordsWithStartUpper(List<Song> songs) {
        HashMap<String, Boolean> hashMap = new HashMap<>();
        for (Song song : songs) {
            for (SongVerse songVerse : song.getVerses()) {
                List<String> splitWithDelimiters = splitWithDelimiters(songVerse.getText());
                boolean previousIsSpace = false;
                for (String s : splitWithDelimiters) {
                    if (previousIsSpace) {
                        System.out.println(s);
                        hashMap.put(s, true);
                    }
                    previousIsSpace = Objects.equals(s, " ");
                }
            }
        }
        return hashMap;
    }

    public static class IVerse {
        String text;
        boolean chorus;
    }

    public static class ISong {
        String title;
        List<IVerse> verses;
    }
}
