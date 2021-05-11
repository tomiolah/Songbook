package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class SetLanguages {
    private static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static HashMap<String, Song> getStringSongHashMap(List<Song> allWithLanguage) {
        HashMap<String, Song> songHashMap = new HashMap<>();
        for (Song song : allWithLanguage) {
            songHashMap.put(song.getUuid(), song);
        }
        return songHashMap;
    }

    private static Map<Language, Collection<String>> getLanguageCollectionMap(List<Language> languages) {
        Map<Language, Collection<String>> languageMap = new HashMap<>();
        for (Language language : languages) {
            TreeSet<String> value = new TreeSet<>();
            languageMap.put(language, value);
        }
        return languageMap;
    }

    public static void setLanguagesForUnknown(SongRepository songRepository, SongService songService, LanguageService languageService) {
        List<Language> languages = languageService.findAll();
        Iterable<Song> songs = songRepository.findAll();
        List<Song> allWithLanguage = songService.findAll();
        HashMap<String, Song> songHashMap = getStringSongHashMap(allWithLanguage);
        Map<Language, Collection<String>> languageMap = getLanguageCollectionMap(languages);
        setLanguagesForUnknownSongs(songRepository, languages, songs, songHashMap, languageMap);
    }

    public static void printLanguagesWords(SongRepository songRepository, LanguageService languageService) {
        List<Language> languages = languageService.findAll();
        Iterable<Song> songRepositoryAll = songRepository.findAll();
        List<Song> allWithLanguage = filterSongsContainingLanguage(songRepositoryAll);
        Map<Language, Collection<String>> languageMap = getLanguageCollectionMap(languages);
        for (Song song : allWithLanguage) {
            if (!song.isDeleted()) {
                addWordByAlreadySettedLanguage(languageMap, song);
            }
        }
        printLanguageWords(languages.get(10), languageMap);
    }

    private static void printLanguageWords(Language language, Map<Language, Collection<String>> languageMap) {
        Collection<String> wordsCollection = languageMap.get(language);
        List<String> sortedWords = getList(wordsCollection);
        Collections.sort(sortedWords);
        String previous = null;
        for (String word : sortedWords) {
            if (!word.equals(previous)) {
                System.out.println(word);
            }
            previous = word;
        }
    }

    private static List<String> getList(Collection<String> stringCollection) {
        return new ArrayList<>(stringCollection);
    }

    private static List<Song> filterSongsContainingLanguage(Iterable<Song> songs) {
        ArrayList<Song> songArrayList = new ArrayList<>();
        for (Song song : songs) {
            if (song.getLanguage() != null) {
                songArrayList.add(song);
            }
        }
        return songArrayList;
    }

    private static void setLanguagesForUnknownSongs(SongRepository songRepository, List<Language> languages, Iterable<Song> songs, HashMap<String, Song> songHashMap, Map<Language, Collection<String>> languageMap) {
        for (Song song : songs) {
            Song song1 = songHashMap.get(song.getUuid());
            if (song1 != null) {
                addWordByAlreadySettedLanguage(languageMap, song1);
            } else {
                if (!song.isJustUploaded()) {
                    continue;
                }
                List<String> words = new ArrayList<>();
                addWordsInCollection(song, words);
                Map<Language, ContainsResult> countMap = new HashMap<>(languages.size());
                for (Language language1 : languages) {
                    Collection<String> wordsByLanguage = languageMap.get(language1);
                    Integer count = 0;
                    Integer wordCount = 0;
                    for (String word : words) {
                        if (wordsByLanguage.contains(word)) {
                            ++count;
                        }
                        ++wordCount;
                    }
                    ContainsResult containsResult = new ContainsResult();
                    containsResult.setCount(count);
                    containsResult.setWordCount(wordCount);
                    countMap.put(language1, containsResult);
                }
                Map.Entry<Language, ContainsResult> max = getMax(countMap);
                printDetailsToConsoleAndSetLanguage(songRepository, languages, languageMap, song, max);
            }
        }
    }

    private static Map.Entry<Language, ContainsResult> getMax(Map<Language, ContainsResult> countMap) {
        Set<Map.Entry<Language, ContainsResult>> entries = countMap.entrySet();
        Map.Entry<Language, ContainsResult> max = new AbstractMap.SimpleEntry<>(null, new ContainsResult());
        for (Map.Entry<Language, ContainsResult> entry : entries) {
            if (entry.getValue().getRatio() > max.getValue().getRatio()) {
                max = entry;
            }
        }
        return max;
    }

    private static void printDetailsToConsoleAndSetLanguage(SongRepository songRepository, List<Language> languages, Map<Language, Collection<String>> languageMap, Song song, Map.Entry<Language, ContainsResult> max) {
        System.out.println(song.getTitle());
        if (max.getKey() != null) {
            System.out.println("Language:   " + max.getKey().getEnglishName());
            System.out.println("Ratio:  " + max.getValue().getRatio());
            System.out.println("Match count:  " + max.getValue().getCount());
            System.out.println("Words:  " + max.getValue().getWordCount());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(">");
        String s;
        try {
            ContainsResult maxValue = max.getValue();
            if ((maxValue.getRatio() > 0.5 && maxValue.getCount() > 40) || (maxValue.getRatio() > 0.7 && maxValue.getCount() > 15)) {
                s = "yes";
            } else {
                s = br.readLine();
            }
            System.out.println(s);
            setLanguageFromConsole(songRepository, languages, languageMap, song, max, s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setLanguageFromConsole(SongRepository songRepository, List<Language> languages, Map<Language, Collection<String>> languageMap, Song song, Map.Entry<Language, ContainsResult> max, String s) {
        switch (s) {
            case "yes":
                setAndSaveLanguage(songRepository, languageMap, song, max.getKey());
                break;
            case "english":
                Language language1 = languages.get(1);
                if (language1.getEnglishName().equals("English")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language1);
                }
                break;
            case "spanish":
                Language language4 = languages.get(0);
                if (language4.getEnglishName().equals("Spanish")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language4);
                }
                break;
            case "roman":
                Language language3 = languages.get(3);
                if (language3.getEnglishName().equals("Romanian")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language3);
                }
                break;
            case "hungarian":
                Language language2 = languages.get(4);
                if (language2.getEnglishName().equals("Hungarian")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language2);
                }
                break;
            case "german":
                Language language5 = languages.get(2);
                if (language5.getEnglishName().equals("German")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language5);
                }
                break;
        }
    }

    private static void setAndSaveLanguage(SongRepository songRepository, Map<Language, Collection<String>> languageMap, Song song, Language language5) {
        song.setLanguage(language5);
        songRepository.save(song);
        addWordsInCollection(song, languageMap.get(song.getLanguage()));
    }

    private static void addWordByAlreadySettedLanguage(Map<Language, Collection<String>> languageMap, Song song1) {
        Language language = song1.getLanguage();
        Collection<String> words = languageMap.get(language);
        addWordsInCollection(song1, words);
    }

    private static void addWordsInCollection(Song song, Collection<String> words) {
        for (SongVerse songVerse : song.getVerses()) {
            String[] split = songVerse.getText().split("[\\s\\t\\n\\r]");
            for (String word : split) {
                word = stripAccents(word.toLowerCase());
                words.add(word);
            }
        }
    }

    private static String stripAccents(String word) {
        String nfdNormalizedString = Normalizer.normalize(word, Normalizer.Form.NFD);
        word = pattern.matcher(nfdNormalizedString).replaceAll("");
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        return word;
    }
}
