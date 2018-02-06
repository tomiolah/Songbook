package projector.service.impl;

import projector.model.Language;
import projector.repository.DAOFactory;
import projector.service.LanguageService;

public class LanguageServiceImpl extends AbstractService<Language> implements LanguageService {
    public LanguageServiceImpl() {
        super(DAOFactory.getInstance().getLanguageDAO());
    }
}
