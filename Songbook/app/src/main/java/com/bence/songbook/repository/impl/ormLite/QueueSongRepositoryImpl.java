package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.QueueSong;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.QueueSongRepository;
import com.bence.songbook.repository.exception.RepositoryException;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class QueueSongRepositoryImpl extends BaseRepositoryImpl<QueueSong> implements QueueSongRepository {
    private static final String TAG = QueueSongRepositoryImpl.class.getSimpleName();

    public QueueSongRepositoryImpl(Context context) {
        super(QueueSong.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            Dao<QueueSong, Long> queueSongDao = databaseHelper.getQueueSongDao();
            super.setDao(queueSongDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize QueueSongRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
