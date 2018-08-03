package projector.repository;

import projector.model.VerseIndex;

import java.util.List;

public interface VerseIndexRepository extends CrudDAO<VerseIndex> {
    List<VerseIndex> findByIndex(Long index);
}
