package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.SongList;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongListRepository;
import com.bence.songbook.repository.exception.RepositoryException;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class SongListRepositoryImpl extends AbstractRepository<SongList> implements SongListRepository {
    private static final String TAG = SongListRepositoryImpl.class.getSimpleName();

    public SongListRepositoryImpl(Context context) {
        super(SongList.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            Dao<SongList, Long> songListDao = databaseHelper.getSongListDao();
            super.setDao(songListDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongListRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
