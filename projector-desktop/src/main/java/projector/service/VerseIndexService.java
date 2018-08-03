package projector.service;

import projector.model.VerseIndex;

import java.util.List;

public interface VerseIndexService extends CrudService<VerseIndex> {
    List<VerseIndex> findByIndex(Long index);
}
