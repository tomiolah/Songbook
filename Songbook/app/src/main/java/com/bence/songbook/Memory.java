package com.bence.songbook;

import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;

import java.util.List;

public class Memory {

    private static Memory instance;
    private List<Song> songs;
    private List<Song> strippedSongs;
    private List<SongCollection> songCollections;

    private Memory() {

    }

    public synchronized static Memory getInstance() {
        if (instance == null) {
            instance = new Memory();
        }
        return instance;
    }

    public synchronized void setInstance(Memory instance) {
        Memory.instance = instance;
    }

    public synchronized List<Song> getSongs() {
        return songs;
    }

    public synchronized void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public synchronized List<Song> getStrippedSongs() {
        return strippedSongs;
    }

    public synchronized void setStrippedSongs(List<Song> strippedSongs) {
        this.strippedSongs = strippedSongs;
    }

    public List<SongCollection> getSongCollections() {
        return songCollections;
    }

    public void setSongCollections(List<SongCollection> songCollections) {
        this.songCollections = songCollections;
    }
}
