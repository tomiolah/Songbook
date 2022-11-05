package projector.service;

import projector.model.Bible;

public interface BibleService extends CrudService<Bible> {
    void checkHasVerseIndices(Bible bible);
}
