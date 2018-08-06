package projector.service.impl;

import projector.model.Bible;
import projector.repository.DAOFactory;
import projector.service.BibleService;

public class BibleServiceImpl extends AbstractBaseService<Bible> implements BibleService {

    public BibleServiceImpl() {
        super(DAOFactory.getInstance().getBibleDAO());
    }
}
