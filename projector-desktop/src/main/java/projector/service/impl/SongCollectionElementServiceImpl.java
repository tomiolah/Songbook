package projector.service.impl;

import projector.model.SongCollectionElement;
import projector.repository.DAOFactory;
import projector.service.SongCollectionElementService;

public class SongCollectionElementServiceImpl extends AbstractBaseService<SongCollectionElement> implements SongCollectionElementService {

    public SongCollectionElementServiceImpl() {
        super(DAOFactory.getInstance().getSongCollectionElementDAO());
    }
}
