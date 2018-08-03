package projector.repository.ormLite;

import com.j256.ormlite.misc.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.VerseIndex;
import projector.repository.RepositoryException;
import projector.repository.VerseIndexRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

class VerseIndexRepositoryImpl extends AbstractRepository<VerseIndex> implements VerseIndexRepository {
    private static final Logger LOG = LoggerFactory.getLogger(VerseIndexRepositoryImpl.class);
    private BookRepositoryImpl bookRepository;

    VerseIndexRepositoryImpl() throws SQLException {
        super(VerseIndex.class, DatabaseHelper.getInstance().getVerseIndexDao());
    }

    @Override
    public List<VerseIndex> create(List<VerseIndex> verseIndices) {
        try {
            TransactionManager.callInTransaction(DatabaseHelper.getInstance().getConnectionSource(),
                    (Callable<Void>) () -> {
                        for (VerseIndex verseIndex : verseIndices) {
                            dao.executeRaw("INSERT INTO VERSEINDEX (INDEXNUMBER,BIBLEVERSE_ID) VALUES ("
                                    + verseIndex.getIndexNumber()
                                    + "," + verseIndex.getBibleVerse().getId()
                                    + ")");
                        }
                        return null;
                    });
        } catch (SQLException e) {
            String msg = "Could not save verseIndices";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
        return verseIndices;
    }

    @Override
    public List<VerseIndex> findByIndex(Long index) {
        String msg = "Could not find index";
        try {
            List<VerseIndex> verseIndices = dao.queryForEq("indexNumber", index);
            for (VerseIndex verseIndex : verseIndices) {
                Chapter chapter = verseIndex.getBibleVerse().getChapter();
                Book book = getBookRepository().findById(chapter.getBook().getId());
                chapter.setBook(book);
            }
            return verseIndices;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    private BookRepositoryImpl getBookRepository() {
        if (bookRepository == null) {
            try {
                bookRepository = new BookRepositoryImpl();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return bookRepository;
    }
}
