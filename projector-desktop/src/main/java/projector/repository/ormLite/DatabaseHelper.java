package projector.repository.ormLite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Information;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongBook;
import projector.model.SongBookSong;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.model.SongVerse;
import projector.repository.RepositoryException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

    private static DatabaseHelper instance;
    private final int DATABASE_VERSION = 1;
    private Dao<Song, Long> songDao;
    private Dao<SongVerse, Long> songVerseDao;
    private ConnectionSource connectionSource;
    private Dao<SongBook, Long> songBookDao;
    private Dao<SongBookSong, Long> songBookSongDao;
    private Dao<Information, Long> informationDao;
    private Dao<SongCollection, Long> songCollectionDao;
    private Dao<SongCollectionElement, Long> songCollectionElementDao;
    private Dao<Language, Long> languageDao;

    private DatabaseHelper() {
        try {
            String DATABASE_URL = "jdbc:h2:./data/projector";
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            int oldVersion = getOldVersion();
            if (oldVersion < DATABASE_VERSION) {
                onUpgrade(connectionSource);
                saveNewVersion();
            }
            onCreate(connectionSource);
        } catch (SQLException e) {
            final String msg = "Unable to create connection";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private void saveNewVersion() {
        try (FileOutputStream stream = new FileOutputStream("data/database.version");
             BufferedWriter br = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {
            br.write(DATABASE_VERSION + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getOldVersion() {
        try (FileInputStream stream = new FileInputStream("data/database.version");
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
            return Integer.parseInt(br.readLine());
        } catch (FileNotFoundException ignored) {
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void onCreate(final ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Language.class);
            TableUtils.createTableIfNotExists(connectionSource, Song.class);
            TableUtils.createTableIfNotExists(connectionSource, SongVerse.class);
            TableUtils.createTableIfNotExists(connectionSource, SongBook.class);
            TableUtils.createTableIfNotExists(connectionSource, SongBookSong.class);
            TableUtils.createTableIfNotExists(connectionSource, Information.class);
            TableUtils.createTableIfNotExists(connectionSource, SongCollection.class);
            TableUtils.createTableIfNotExists(connectionSource, SongCollectionElement.class);
            try {
                getSongVerseDao().executeRaw("ALTER TABLE `SONGVERSE` ADD COLUMN secondText VARCHAR(1000);");
            } catch (Exception ignored) {
            }
        } catch (final SQLException e) {
            LOG.error("Unable to create databases", e);
        }
    }

    private void onUpgrade(final ConnectionSource connectionSource) {
        try {
            TableUtils.dropTable(connectionSource, Song.class, true);
            TableUtils.dropTable(connectionSource, SongVerse.class, true);
            TableUtils.dropTable(connectionSource, SongBook.class, true);
            TableUtils.dropTable(connectionSource, SongBookSong.class, true);
            TableUtils.dropTable(connectionSource, Information.class, true);
            TableUtils.dropTable(connectionSource, SongCollection.class, true);
            TableUtils.dropTable(connectionSource, SongCollectionElement.class, true);
            TableUtils.dropTable(connectionSource, Language.class, true);
        } catch (final Exception e) {
            LOG.error("Unable to upgrade database", e);
        }
        try {
            onCreate(connectionSource);
        } catch (final Exception e) {
            LOG.error("Unable to create databases", e);
        }
    }

    Dao<Song, Long> getSongDao() throws SQLException {
        if (songDao == null) {
            songDao = DaoManager.createDao(connectionSource, Song.class);
        }
        return songDao;
    }

    Dao<SongVerse, Long> getSongVerseDao() throws SQLException {
        if (songVerseDao == null) {
            songVerseDao = DaoManager.createDao(connectionSource, SongVerse.class);
        }
        return songVerseDao;
    }

    Dao<SongBook, Long> getSongBookDao() throws SQLException {
        if (songBookDao == null) {
            songBookDao = DaoManager.createDao(connectionSource, SongBook.class);
        }
        return songBookDao;
    }

    Dao<SongBookSong, Long> getSongBookSongDao() throws SQLException {
        if (songBookSongDao == null) {
            songBookSongDao = DaoManager.createDao(connectionSource, SongBookSong.class);
        }
        return songBookSongDao;
    }

    Dao<Information, Long> getInformationDao() throws SQLException {
        if (informationDao == null) {
            informationDao = DaoManager.createDao(connectionSource, Information.class);
        }
        return informationDao;
    }

    public Dao<SongCollection, Long> getSongCollectionDao() throws SQLException {
        if (songCollectionDao == null) {
            songCollectionDao = DaoManager.createDao(connectionSource, SongCollection.class);
        }
        return songCollectionDao;
    }

    public Dao<SongCollectionElement, Long> getSongCollectionElementDao() throws SQLException {
        if (songCollectionElementDao == null) {
            songCollectionElementDao = DaoManager.createDao(connectionSource, SongCollectionElement.class);
        }
        return songCollectionElementDao;
    }

    public Dao<Language, Long> getLanguageDao() throws SQLException {
        if (languageDao == null) {
            languageDao = DaoManager.createDao(connectionSource, Language.class);
        }
        return languageDao;
    }

    public void close() {
        songDao = null;
        songVerseDao = null;
        songBookDao = null;
        songBookSongDao = null;
        informationDao = null;
        songCollectionDao = null;
        songCollectionElementDao = null;
        languageDao = null;
        try {
            connectionSource.close();
        } catch (SQLException e) {
            final String msg = "Cannot close connection";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }
}
