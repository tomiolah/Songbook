package com.bence.projector.server.api.resources.seo;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class SongResourceSeo {
    private final SongService songService;
    private List<Song> songs;

    @Autowired
    public SongResourceSeo(SongService songService) {
        this.songService = songService;
    }

    @GetMapping("/song/{id}")
    public String song(Model model, @PathVariable("id") String id) {
        Song song = songService.findOne(id);
        if (song == null || song.isDeleted()) {
            return "pageNotFound";
        }
        model.addAttribute("title", song.getTitle());
        model.addAttribute("song", song);
        Song randomSong = songService.getRandomSong();
        model.addAttribute("randomSongUrl", "/song/" + randomSong.getId());
        HashMap<String, Integer> hashMap = new HashMap<>();
        String regex = "[.!?,;'\\\\:\"|<>{}\\[\\]_\\-=+0-9@#$%^&*()`~\\n]+";
        for (SongVerse songVerse : song.getVerses()) {
            for (String s : songVerse.getText().replaceAll(regex, " ").split(" ")) {
                String lowerCase = s.trim().toLowerCase();
                if (lowerCase.length() > 2) {
                    Integer integer = hashMap.get(lowerCase);
                    if (integer == null || integer == 0) {
                        hashMap.put(lowerCase, 1);
                    } else {
                        hashMap.replace(lowerCase, integer + 1);
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> list = new LinkedList<>(hashMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        Set<String> strings = sortedMap.keySet();
        StringBuilder keywords = new StringBuilder("lyrics");
        String englishName = song.getLanguage().getEnglishName();
        switch (englishName) {
            case "Hungarian":
                keywords.append(", dalszöveg, szöveg, ének");
                break;
            case "Romanian":
                keywords.append(", versuri, text, cântec, imn");
                break;
            case "German":
                keywords.append(", text, lied, hymne");
                break;
            default: //if (englishName.equals("English"))
                keywords.append(", text, hymn, song");
                break;
        }
        Iterator<String> iterator = strings.iterator();
        for (int i = 0; iterator.hasNext() && i < 4; ++i) {
            keywords.append(", ").append(iterator.next());
        }
        keywords.append(", ").append(song.getTitle().replaceAll(regex, ""));

        model.addAttribute("keywords", keywords.toString());
        model.addAttribute("youtubeUrl", "http://img.youtube.com/vi/" + song.getYoutubeUrl() + "/0.jpg");
        return "song";
    }

    @GetMapping("/song")
    public String songs(Model model) {
        List<Song> songs = getSongs();
        model.addAttribute("songs", songs);
        return "songs";
    }

    private List<Song> getSongs() {
        if (songs == null) {
            songs = songService.findAll();
            for (Song song : songs) {
                song.setId("../song/" + song.getId());
            }
        }
        return songs;
    }
}
