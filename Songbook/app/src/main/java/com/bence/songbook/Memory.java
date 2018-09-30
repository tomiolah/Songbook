package com.bence.songbook;

import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class Memory {

    private static Memory instance;
    private List<Song> songs;
    private List<SongCollection> songCollections;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private boolean shareOnNetwork;
    private List<Song> values;
    private MainActivity mainActivity;
    private Song songForLinking;
    private Song passingSong;
    private List<String> sharedTexts;

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

    public Song getSongForLinking() {
        return songForLinking;
    }

    public void setSongForLinking(Song songForLinking) {
        this.songForLinking = songForLinking;
    }

    public Song getPassingSong() {
        return passingSong;
    }

    public void setPassingSong(Song passingSong) {
        this.passingSong = passingSong;
    }

    public List<String> getSharedTexts() {
        if (sharedTexts == null) {
            sharedTexts = new ArrayList<>();
        }
        return sharedTexts;
    }

    public void setSharedTexts(List<String> sharedTexts) {
        this.sharedTexts = sharedTexts;
    }
}
