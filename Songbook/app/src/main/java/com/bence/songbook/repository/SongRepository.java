package com.bence.songbook.repository;

import android.widget.ProgressBar;

import com.bence.songbook.models.Song;

import java.util.List;

public interface SongRepository extends BaseRepository<Song> {
    void save(List<Song> newSongs, ProgressBar progressBar);

    Song findByUUID(String uuid);
}
