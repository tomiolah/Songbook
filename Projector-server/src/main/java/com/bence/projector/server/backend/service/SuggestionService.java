package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Suggestion;

import java.util.List;

public interface SuggestionService extends BaseService<Suggestion> {
    List<Suggestion> findAllByLanguage(Language language);
}
