package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.bence.songbook.models.Song;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.SongVerseRepository;
import com.bence.songbook.repository.exception.RepositoryException;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class SongRepositoryImpl extends AbstractRepository<Song> implements SongRepository {
    private static final String TAG = SongRepositoryImpl.class.getSimpleName();

    private Dao<Song, Long> songDao;
    private SongVerseRepository songVerseRepository;

    public SongRepositoryImpl(final Context context) {
        super(Song.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            songDao = databaseHelper.getSongDao();
            super.setDao(songDao);
            songVerseRepository = new SongVerseRepositoryImpl(context);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public Song findOne(final Long id) {
        try {
            return songDao.queryForId(id);
        } catch (SQLException e) {
            String msg = "Could not find song";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<Song> findAll() {
        try {
            return songDao.queryForAll();
        } catch (final SQLException e) {
            String msg = "Could not find all songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final Song song) {
        try {
            if (!song.isDeleted()) {
                songDao.createOrUpdate(song);
                songVerseRepository.save(song.getVerses());
            }
        } catch (SQLException e) {
            String msg = "Could not save song";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final List<Song> songs) {
        try {
            songDao.callBatchTasks(
                    new Callable<Void>() {
                        public Void call() throws SQLException {
                            for (final Song song : songs) {
                                save(song);
                            }
                            return null;
                        }
                    });
        } catch (RepositoryException e) {
            String msg = "Could not save songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            String msg = "Could not save songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final List<Song> newSongs, final ProgressBar progressBar) {
        try {
            songDao.callBatchTasks(
                    new Callable<Void>() {
                        public Void call() throws SQLException {
                            int i = 0;
                            for (final Song song : newSongs) {
                                save(song);
                                progressBar.setProgress(++i);
                            }
                            return null;
                        }
                    });
        } catch (RepositoryException e) {
            String msg = "Could not save songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            String msg = "Could not save songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public Song findByUUID(String uuid) {
        String msg = "Could not find song";
        try {
            ArrayList<Song> uuid1 = (ArrayList<Song>) songDao.queryForEq("uuid", uuid);
            if (uuid1 != null && uuid1.size() > 0) {
                return uuid1.get(0);
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

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        String msg = "Could not find song versions";
        try {
            List<Song> songs = songDao.queryForEq("versionGroup", versionGroup);
            Song byUUID = findByUUID(versionGroup);
            if (byUUID != null) {
                songs.add(byUUID);
            }
            return songs;
        } catch (SQLException e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void delete(final Song song) {
        try {
            if (song != null) {
                songDao.deleteById(song.getId());
            }
        } catch (SQLException e) {
            String msg = "Could not delete song";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

}
