package projector.service.impl;

import projector.model.Bible;
import projector.repository.DAOFactory;
import projector.repository.ormLite.VerseIndexRepositoryImpl;
import projector.service.BibleService;

import java.sql.SQLException;

public class BibleServiceImpl extends AbstractBaseService<Bible> implements BibleService {
    private final VerseIndexRepositoryImpl verseIndexRepository;

    public BibleServiceImpl() {
        super(DAOFactory.getInstance().getBibleDAO());
        try {
            verseIndexRepository = new VerseIndexRepositoryImpl();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkHasVerseIndices(Bible bible) {
        if (bible == null || bible.hasVerseIndicesChecked()) {
            return;
        }
        bible.setHasVerseIndices(verseIndexRepository.countByBibleId(bible.getId()) > 0);
        bible.setHasVerseIndicesChecked(true);
    }
}
