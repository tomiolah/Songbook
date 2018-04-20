package com.bence.songbook;

import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.ui.activity.MainActivity;

import java.util.List;

public class Memory {

    private static Memory instance;
    private List<Song> songs;
    private List<SongCollection> songCollections;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private boolean shareOnNetwork;
    private List<Song> values;
    private MainActivity mainActivity;
    private Song song;
    private Song songForLinking;

    private Memory() {

    }

    public synchronized static Memory getInstance() {
        if (instance == null) {
            instance = new Memory();
        }
        return instance;
    }

    public synchronized List<Song> getSongs() {
        return songs;
    }

    public synchronized void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public List<SongCollection> getSongCollections() {
        return songCollections;
    }

    public void setSongCollections(List<SongCollection> songCollections) {
        this.songCollections = songCollections;
    }

    public List<ProjectionTextChangeListener> getProjectionTextChangeListeners() {
        return projectionTextChangeListeners;
    }

    public void setProjectionTextChangeListeners(List<ProjectionTextChangeListener> projectionTextChangeListeners) {
        this.projectionTextChangeListeners = projectionTextChangeListeners;
    }

    public boolean isShareOnNetwork() {
        return shareOnNetwork;
    }

    public void setShareOnNetwork(boolean shareOnNetwork) {
        this.shareOnNetwork = shareOnNetwork;
    }

    public List<Song> getValues() {
        return values;
    }

    public void setValues(List<Song> values) {
        this.values = values;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Song getSongForLinking() {
        return songForLinking;
    }

    public void setSongForLinking(Song songForLinking) {
        this.songForLinking = songForLinking;
    }
}
