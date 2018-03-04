package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongCollectionElementRepository;
import com.bence.songbook.repository.exception.RepositoryException;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

public class SongCollectionElementRepositoryImpl extends AbstractRepository<SongCollectionElement> implements SongCollectionElementRepository {
    private static final String TAG = SongCollectionElementRepositoryImpl.class.getSimpleName();

    public SongCollectionElementRepositoryImpl(Context context) {
        super(SongCollectionElement.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            Dao<SongCollectionElement, Long> songCollectionDao = databaseHelper.getSongCollectionElementDao();
            super.setDao(songCollectionDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongCollectionRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(List<SongCollectionElement> songCollectionElements, ProgressBar progressBar) {
        progressBar.setMax(songCollectionElements.size());
        int i = 0;
        for (SongCollectionElement songCollectionElement : songCollectionElements) {
            save(songCollectionElement);
            progressBar.setProgress(++i);
        }
    }
}