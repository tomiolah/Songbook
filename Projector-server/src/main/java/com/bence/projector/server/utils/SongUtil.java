package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;

import java.util.List;

public class SongUtil {

    public static Song getLastModifiedSong(List<Song> songs) {
        if (songs == null || songs.size() == 0) {
            return null;
        }
        Song lastModifiedSong = songs.get(0);
        for (Song song : songs) {
            if (lastModifiedSong.getModifiedDate().before(song.getModifiedDate())) {
                lastModifiedSong = song;
            }
        }
        return lastModifiedSong;
    }
}
