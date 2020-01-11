package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.repository.LanguageRepository;
import com.bence.projector.server.backend.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
public class LanguageServiceImpl extends BaseServiceImpl<Language> implements LanguageService {
    private final MongoTemplate mongoTemplate;
    private final Map<String, Language> languageMap = new HashMap<>();
    private final LanguageRepository languageRepository;

    @Autowired
    public LanguageServiceImpl(MongoTemplate mongoTemplate, LanguageRepository languageRepository) {
        this.mongoTemplate = mongoTemplate;
        this.languageRepository = languageRepository;
    }

    @Override
    public long countSongsById(String id) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(where("id").is(id)),
                Aggregation.project().and("songs").project("size").as("count"));
        AggregationResults<LanguageCount> groupResults = mongoTemplate.aggregate(aggregation, Language.class, LanguageCount.class);
        List<LanguageCount> result = groupResults.getMappedResults();
        return result.get(0).getCount();
    }

    @Override
    public void sortBySize(List<Language> languages) {
        for (Language language : languages) {
            language.setSongsCount(countSongsById(language.getId()));
        }
        languages.sort((o1, o2) -> Long.compare(o2.getSongsCount(), o1.getSongsCount()));
    }

    @Override
    public Language findLanguageBySongsContaining(Song song) {
        Language languageBySongsContaining = languageRepository.findLanguageBySongsContaining(song);
        if (languageBySongsContaining == null) {
            return null;
        }
        return findOne(languageBySongsContaining.getId());
    }

    @Override
    public List<Language> findAll() {
        List<Language> languages = languageRepository.findAll();
        List<Language> allLanguages = new ArrayList<>(languages.size());
        for (Language language : languages) {
            allLanguages.add(findOne(language.getId()));
        }
        return allLanguages;
    }

    @Override
    public Language findOne(String id) {
        if (languageMap.containsKey(id)) {
            return languageMap.get(id);
        }
        Language language = super.findOne(id);
        languageMap.put(id, language);
        return language;
    }
}
