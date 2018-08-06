package projector.repository.ormLite;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.misc.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.BibleVerse;
import projector.repository.BibleVerseRepository;
import projector.repository.RepositoryException;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class BibleVerseRepositoryImpl extends AbstractBaseRepository<BibleVerse> implements BibleVerseRepository {
    private static final Logger LOG = LoggerFactory.getLogger(BibleVerseRepositoryImpl.class);
    private VerseIndexRepositoryImpl verseIndexRepository;

    BibleVerseRepositoryImpl() throws SQLException {
        super(BibleVerse.class, DatabaseHelper.getInstance().getBibleVerseDao());
        try {
            verseIndexRepository = new VerseIndexRepositoryImpl();
        } catch (SQLException e) {
            String msg = "Failed to initialize";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<BibleVerse> create(List<BibleVerse> bibleVerses) {
        try {
            GenericRawResults<String[]> strings = dao.queryRaw("SELECT max(id) FROM `bibleverse`");
            long max;
            try {
                max = Long.parseLong(strings.getFirstResult()[0]);
            } catch (NumberFormatException e) {
                max = 0L;
            }
            long finalMax = max;
            TransactionManager.callInTransaction(DatabaseHelper.getInstance().getConnectionSource(),
                    (Callable<Void>) () -> {
                        Long id = finalMax + 1;
                        for (BibleVerse bibleVerse : bibleVerses) {
                            bibleVerse.setId(id);
                            dao.executeRaw("INSERT INTO BIBLEVERSE (ID,Text,StrippedText,Chapter_Id,Number) VALUES (" + id++
                                    + ",'" + bibleVerse.getText().replaceAll("'", "''")
                                    + "','" + bibleVerse.getStrippedText()
                                    + "'," + bibleVerse.getChapter().getId()
                                    + "," + bibleVerse.getNumber()
                                    + ")");
                        }
                        return null;
                    });
        } catch (SQLException e) {
            String msg = "Could not save bibleVerses";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
        return bibleVerses;
    }

    @Override
    public boolean delete(BibleVerse model) throws RepositoryException {
        super.delete(model);
        verseIndexRepository.deleteAll(model.getVerseIndices());
        return true;
    }

    @Override
    public boolean deleteAll(List<BibleVerse> models) throws RepositoryException {
        for (BibleVerse verse : models) {
            delete(verse);
        }
        return true;
    }
}
