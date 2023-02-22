package projector.repository.ormLite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.CountdownTime;
import projector.model.Information;
import projector.model.Language;
import projector.model.LoggedInUser;
import projector.model.Song;
import projector.model.SongBook;
import projector.model.SongBookSong;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.model.SongVerse;
import projector.model.VerseIndex;
import projector.repository.RepositoryException;
import projector.repository.dao.CustomDao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

    private static DatabaseHelper instance;
    private final int DATABASE_VERSION = 17;
    private final ConnectionSource connectionSource;
    private Dao<Song, Long> songDao;
    private Dao<SongVerse, Long> songVerseDao;
    private Dao<SongBook, Long> songBookDao;
    private Dao<SongBookSong, Long> songBookSongDao;
    private Dao<Information, Long> informationDao;
    private Dao<SongCollection, Long> songCollectionDao;
    private Dao<SongCollectionElement, Long> songCollectionElementDao;
    private Dao<Language, Long> languageDao;
    private Dao<Bible, Long> bibleDao;
    private Dao<Book, Long> bookDao;
    private Dao<Chapter, Long> chapterDao;
    private Dao<BibleVerse, Long> bibleVerseDao;
    private Dao<VerseIndex, Long> verseIndexDao;
    private Dao<CountdownTime, Long> countdownTimeDao;
    private CustomDao<LoggedInUser, Long> loggedInUserDao;

    private DatabaseHelper() {
        try {
            String DATABASE_URL = "jdbc:h2:./data/projector";
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            int oldVersion = getOldVersion();
            if (oldVersion < DATABASE_VERSION) {
                if (oldVersion < 1) {
                    onUpgrade(connectionSource);
                } else if (oldVersion == 3) {
                    try {
                        TableUtils.dropTable(connectionSource, Bible.class, true);
                        TableUtils.dropTable(connectionSource, Book.class, true);
                        TableUtils.dropTable(connectionSource, Chapter.class, true);
                        TableUtils.dropTable(connectionSource, BibleVerse.class, true);
                        TableUtils.dropTable(connectionSource, VerseIndex.class, true);
                    } catch (Exception ignored) {
                    }
                } else if (oldVersion == 4) {
                    //noinspection TextBlockMigration
                    getSongCollectionElementDao().executeRaw("DELETE FROM SONGCOLLECTIONELEMENT \n" +
                            " WHERE SONGCOLLECTION_ID NOT IN (SELECT f.id \n" +
                            "                        FROM SONGCOLLECTION f)");
                }
                if (oldVersion <= 7) {
                    Dao<Song, Long> songDao = getSongDao();
                    try {
                        songDao.executeRaw("ALTER TABLE `song` ADD COLUMN views INTEGER");
                    } catch (Exception ignored) {
                    }
                    try {
                        songDao.executeRaw("ALTER TABLE `song` ADD COLUMN favouriteCount INTEGER");
                    } catch (Exception ignored) {
                    }
                    Dao<Bible, Long> bibleDao = getBibleDao();
                    try {
                        bibleDao.executeRaw("ALTER TABLE `bible` ADD COLUMN showAbbreviation INTEGER");
                    } catch (Exception ignored) {
                    }
                }
                if (oldVersion <= 8) {
                    Dao<SongCollection, Long> songCollectionDao = getSongCollectionDao();
                    try {
                        songCollectionDao.executeRaw("ALTER TABLE `songCollection` ADD COLUMN needUpload BOOLEAN");
                    } catch (Exception ignored) {
                    }
                }
                if (oldVersion <= 9) {
                    Dao<Song, Long> songDao = getSongDao();
                    try {
                        songDao.executeRaw("ALTER TABLE `song` ADD COLUMN author VARCHAR(100)");
                    } catch (Exception ignored) {
                    }
                }
                if (oldVersion <= 10) {
                    Dao<Song, Long> songDao = getSongDao();
                    try {
                        songDao.executeRaw("ALTER TABLE `song` ADD COLUMN verseOrder VARCHAR(100)");
                    } catch (Exception ignored) {
                    }
                    Dao<SongVerse, Long> songVerseDao = getSongVerseDao();
                    try {
                        songVerseDao.executeRaw("ALTER TABLE `songVerse` ADD COLUMN sectionTypeData INTEGER");
                    } catch (Exception ignored) {
                    }
                }
                if (oldVersion <= 11) {
                    Dao<Song, Long> songDao = getSongDao();
                    try {
                        songDao.executeRaw("ALTER TABLE `song` MODIFY verseOrder VARCHAR(300)");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (oldVersion <= 12) {
                    Dao<Song, Long> songDao = getSongDao();
                    try {
                        songDao.executeRaw("ALTER TABLE `song` ADD COLUMN downloadedSeparately BOOLEAN");
                    } catch (Exception ignored) {
                    }
                }
                if (oldVersion <= 13) {
                    Dao<Song, Long> songDao = getSongDao();
                    try {
                        songDao.executeRaw("ALTER TABLE `SONG` ADD COLUMN versionGroup_temp VARCHAR(36);");
                        songDao.executeRaw("UPDATE song SET versionGroup_temp = versionGroup;");
                        songDao.executeRaw("ALTER TABLE `SONG` DROP COLUMN versionGroup;");
                        songDao.executeRaw("ALTER TABLE `SONG` ADD COLUMN versionGroup VARCHAR(36);");
                        songDao.executeRaw("UPDATE song SET versionGroup = versionGroup_temp;");
                        songDao.executeRaw("ALTER TABLE `SONG` DROP COLUMN versionGroup_temp;");
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                if (oldVersion <= 14) {
                    Dao<Bible, Long> bibleDao = getBibleDao();
                    try {
                        bibleDao.executeRaw("ALTER TABLE `bible` ADD COLUMN preferredByRemote INTEGER");
                    } catch (Exception ignored) {
                    }
                }
                if (oldVersion <= 15) {
                    Dao<SongCollection, Long> songCollectionDao = getSongCollectionDao();
                    try {
                        songCollectionDao.executeRaw("ALTER TABLE `songCollection` ADD COLUMN showInTitle BOOLEAN");
                    } catch (Exception ignored) {
                    }
                }
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
             BufferedWriter br = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
            br.write(DATABASE_VERSION + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getOldVersion() {
        try (FileInputStream stream = new FileInputStream("data/database.version");
             BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
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
            TableUtils.createTableIfNotExists(connectionSource, Bible.class);
            TableUtils.createTableIfNotExists(connectionSource, Book.class);
            TableUtils.createTableIfNotExists(connectionSource, Chapter.class);
            TableUtils.createTableIfNotExists(connectionSource, BibleVerse.class);
            TableUtils.createTableIfNotExists(connectionSource, VerseIndex.class);
            TableUtils.createTableIfNotExists(connectionSource, CountdownTime.class);
            TableUtils.createTableIfNotExists(connectionSource, LoggedInUser.class);
            try {
                getSongVerseDao().executeRaw("ALTER TABLE `SONGVERSE` ADD COLUMN secondText VARCHAR(1000);");
            } catch (Exception ignored) {
            }
            try {
                getSongDao().executeRaw("ALTER TABLE `SONG` ADD COLUMN versionGroup VARCHAR(36);");
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
            TableUtils.dropTable(connectionSource, Bible.class, true);
            TableUtils.dropTable(connectionSource, Book.class, true);
            TableUtils.dropTable(connectionSource, Chapter.class, true);
            TableUtils.dropTable(connectionSource, BibleVerse.class, true);
            TableUtils.dropTable(connectionSource, VerseIndex.class, true);
            TableUtils.dropTable(connectionSource, CountdownTime.class, true);
            TableUtils.dropTable(connectionSource, LoggedInUser.class, true);
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

    Dao<SongCollection, Long> getSongCollectionDao() throws SQLException {
        if (songCollectionDao == null) {
            songCollectionDao = DaoManager.createDao(connectionSource, SongCollection.class);
        }
        return songCollectionDao;
    }

    Dao<SongCollectionElement, Long> getSongCollectionElementDao() throws SQLException {
        if (songCollectionElementDao == null) {
            songCollectionElementDao = DaoManager.createDao(connectionSource, SongCollectionElement.class);
        }
        return songCollectionElementDao;
    }

    Dao<Language, Long> getLanguageDao() throws SQLException {
        if (languageDao == null) {
            languageDao = DaoManager.createDao(connectionSource, Language.class);
        }
        return languageDao;
    }

    Dao<Bible, Long> getBibleDao() throws SQLException {
        if (bibleDao == null) {
            bibleDao = DaoManager.createDao(connectionSource, Bible.class);
        }
        return bibleDao;
    }

    Dao<Book, Long> getBookDao() throws SQLException {
        if (bookDao == null) {
            bookDao = DaoManager.createDao(connectionSource, Book.class);
        }
        return bookDao;
    }

    Dao<Chapter, Long> getChapterDao() throws SQLException {
        if (chapterDao == null) {
            chapterDao = DaoManager.createDao(connectionSource, Chapter.class);
        }
        return chapterDao;
    }

    Dao<BibleVerse, Long> getBibleVerseDao() throws SQLException {
        if (bibleVerseDao == null) {
            bibleVerseDao = DaoManager.createDao(connectionSource, BibleVerse.class);
        }
        return bibleVerseDao;
    }

    Dao<VerseIndex, Long> getVerseIndexDao() throws SQLException {
        if (verseIndexDao == null) {
            verseIndexDao = DaoManager.createDao(connectionSource, VerseIndex.class);
        }
        return verseIndexDao;
    }

    Dao<CountdownTime, Long> getCountdownTimeDao() throws SQLException {
        if (countdownTimeDao == null) {
            countdownTimeDao = DaoManager.createDao(connectionSource, CountdownTime.class);
        }
        return countdownTimeDao;
    }

    public CustomDao<LoggedInUser, Long> getLoggedInUserDao() throws SQLException {
        if (loggedInUserDao == null) {
            loggedInUserDao = getCustomDao(LoggedInUser.class);
        }
        return loggedInUserDao;
    }

    @SuppressWarnings("SameParameterValue")
    private <T> CustomDao<T, Long> getCustomDao(Class<T> aClass) throws SQLException {
        return new CustomDao<>(DaoManager.createDao(connectionSource, aClass));
    }

    ConnectionSource getConnectionSource() {
        return connectionSource;
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
        bibleDao = null;
        bookDao = null;
        chapterDao = null;
        bibleVerseDao = null;
        verseIndexDao = null;
        try {
            connectionSource.close();
        } catch (SQLException e) {
            final String msg = "Cannot close connection";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }
}
