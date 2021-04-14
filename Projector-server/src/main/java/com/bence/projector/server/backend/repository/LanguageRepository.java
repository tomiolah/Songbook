package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import org.springframework.data.repository.CrudRepository;

public interface LanguageRepository extends CrudRepository<Language, Long> {

    Language findLanguageBySongsContaining(Song song);

    Language findOneByUuid(String uuid);
}
