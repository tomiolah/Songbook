package com.bence.songbook.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bence.songbook.R;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongVerse;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "songbook.db";
    private static final int DATABASE_VERSION = 12;

    @SuppressLint("StaticFieldLeak")
    private static DatabaseHelper instance;
    private Context context;

    private Dao<Song, Long> songDao;
    private Dao<SongVerse, Long> songVerseDao;
    private Dao<Language, Long> languageDao;
    private Dao<SongCollection, Long> songCollectionDao;
    private Dao<SongCollectionElement, Long> songCollectionElementDao;
    private Dao<FavouriteSong, Long> favouriteSongDao;

    private DatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    public static DatabaseHelper getInstance(final Context context) {
        if (instance == null) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.context = context;
            instance = databaseHelper;
        }
        return instance;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqliteDatabase,
                         final ConnectionSource connectionSource) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putInt("songDataBaseVersion", 8).apply();
            TableUtils.createTableIfNotExists(connectionSource, Song.class);
            sharedPreferences.edit().putInt("songVerseDataBaseVersion", 4).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongVerse.class);
            sharedPreferences.edit().putInt("languageDataBaseVersion", 4).apply();
            TableUtils.createTableIfNotExists(connectionSource, Language.class);
            sharedPreferences.edit().putInt("songCollectionDataBaseVersion", 5).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongCollection.class);
            sharedPreferences.edit().putInt("songCollectionElementDataBaseVersion", 5).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongCollectionElement.class);
            sharedPreferences.edit().putInt("favouriteSongDataBaseVersion", 1).apply();
            TableUtils.createTableIfNotExists(connectionSource, FavouriteSong.class);
        } catch (final SQLException e) {
            Log.e(TAG, "Unable to create databases", e);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqliteDatabase,
                          final ConnectionSource connectionSource, final int oldVer, final int newVer) {
        try {
            if (oldVer < newVer) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                int songDataBaseVersion = sharedPreferences.getInt("songDataBaseVersion", 0);
                if (songDataBaseVersion < 4) {
                    TableUtils.dropTable(connectionSource, Song.class, true);
                } else {
                    if (songDataBaseVersion == 4) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN versionGroup VARCHAR(30);");
                    }
                    if (songDataBaseVersion < 6) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN youtubeUrl VARCHAR(20);");
                    }
                    if (songDataBaseVersion == 7) {
                        try {
                            getSongDao().executeRaw("ALTER TABLE `song` DROP COLUMN favourite ;");
                            getSongDao().executeRaw("ALTER TABLE `song` DROP COLUMN favouritePublished ;");
                        } catch (Exception ignored) {
                        }
                    }
                }
                int songVerseDataBaseVersion = sharedPreferences.getInt("songVerseDataBaseVersion", 0);
                if (songVerseDataBaseVersion < 4) {
                    TableUtils.dropTable(connectionSource, SongVerse.class, true);
                }
                int languageDataBaseVersion = sharedPreferences.getInt("languageDataBaseVersion", 0);
                if (languageDataBaseVersion < 4) {
                    TableUtils.dropTable(connectionSource, Language.class, true);
                }
                int songCollectionDataBaseVersion = sharedPreferences.getInt("songCollectionDataBaseVersion", 0);
                if (songCollectionDataBaseVersion < 5) {
                    TableUtils.dropTable(connectionSource, SongCollection.class, true);
                }
                int songCollectionElementDataBaseVersion = sharedPreferences.getInt("songCollectionElementDataBaseVersion", 0);
                if (songCollectionElementDataBaseVersion < 5) {
                    TableUtils.dropTable(connectionSource, SongCollectionElement.class, true);
                }
                int favouriteSongDataBaseVersion = sharedPreferences.getInt("favouriteSongDataBaseVersion", 0);
                if (favouriteSongDataBaseVersion < 0) {
                    TableUtils.dropTable(connectionSource, FavouriteSong.class, true);
                }
            }
        } catch (final Exception e) {
            Log.e(TAG,
                    "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
        }
        try {
            onCreate(sqliteDatabase, connectionSource);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to create databases", e);
        }
    }

    public Dao<Song, Long> getSongDao() throws SQLException {
        if (songDao == null) {
            songDao = getDao(Song.class);
        }
        return songDao;
    }

    public Dao<SongVerse, Long> getSongVerseDao() throws SQLException {
        if (songVerseDao == null) {
            songVerseDao = getDao(SongVerse.class);
        }
        return songVerseDao;
    }

    public Dao<Language, Long> getLanguageDao() throws SQLException {
        if (languageDao == null) {
            languageDao = getDao(Language.class);
        }
        return languageDao;
    }

    public Dao<SongCollection, Long> getSongCollectionDao() throws SQLException {
        if (songCollectionDao == null) {
            songCollectionDao = getDao(SongCollection.class);
        }
        return songCollectionDao;
    }

    public Dao<SongCollectionElement, Long> getSongCollectionElementDao() throws SQLException {
        if (songCollectionElementDao == null) {
            songCollectionElementDao = getDao(SongCollectionElement.class);
        }
        return songCollectionElementDao;
    }

    public Dao<FavouriteSong, Long> getFavouriteSongDao() throws SQLException {
        if (favouriteSongDao == null) {
            favouriteSongDao = getDao(FavouriteSong.class);
        }
        return favouriteSongDao;
    }

    @Override
    public void close() {
        super.close();
        songDao = null;
        songVerseDao = null;
        languageDao = null;
        songCollectionDao = null;
        songCollectionElementDao = null;
        favouriteSongDao = null;
    }
}
