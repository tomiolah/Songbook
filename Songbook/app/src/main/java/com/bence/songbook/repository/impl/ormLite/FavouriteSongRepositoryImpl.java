package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.exception.RepositoryException;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

public class FavouriteSongRepositoryImpl extends BaseRepositoryImpl<FavouriteSong> implements FavouriteSongRepository {
    private static final String TAG = FavouriteSongRepositoryImpl.class.getSimpleName();
    private final Dao<FavouriteSong, Long> favouriteSongDao;

    public FavouriteSongRepositoryImpl(Context context) {
        super(FavouriteSong.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            favouriteSongDao = databaseHelper.getFavouriteSongDao();
            super.setDao(favouriteSongDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize FavouriteSongRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public FavouriteSong findFavouriteSongBySongUuid(String uuid) {
        String msg = "Could not find favouriteSong";
        try {
            ArrayList<FavouriteSong> favouriteSongs = (ArrayList<FavouriteSong>) favouriteSongDao.queryForEq("songUuid", uuid);
            if (favouriteSongs != null && favouriteSongs.size() > 0) {
                return favouriteSongs.get(0);
            }
            return null;
        } catch (SQLException e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
