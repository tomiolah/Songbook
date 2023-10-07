package projector.service.impl;

import projector.model.Bible;
import projector.model.Language;
import projector.repository.DAOFactory;
import projector.repository.ormLite.VerseIndexRepositoryImpl;
import projector.service.BibleService;
import projector.service.ServiceManager;

import java.sql.SQLException;
import java.util.List;

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

    @Override
    public void sort(List<Bible> bibles) {
        String songSelectedLanguageUuid = ServiceManager.getLanguageService().getSongSelectedLanguageUuid();
        bibles.sort((o2, o1) -> {
            Language o2Language = o2.getLanguage();
            Language o1Language = o1.getLanguage();
            if (o1Language != null && o2Language != null) {
                if (songSelectedLanguageUuid != null) {
                    if (o2Language.getUuid().equals(songSelectedLanguageUuid)) {
                        return -1;
                    }
                    if (o1Language.getUuid().equals(songSelectedLanguageUuid)) {
                        return 1;
                    }
                }
                int selectedCompare = Boolean.compare(o1Language.isSelected(), o2Language.isSelected());
                if (selectedCompare != 0) {
                    return selectedCompare;
                }
                return o2Language.getUuid().compareTo(o1Language.getUuid());
            }
            return 0;
        });
    }
}
