package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.server.api.assembler.LanguageAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class LanguageResource {

    private final LanguageService languageService;
    private final LanguageAssembler languageAssembler;
    private final StatisticsService statisticsService;

    @Autowired
    public LanguageResource(LanguageService languageService, LanguageAssembler languageAssembler, StatisticsService statisticsService) {
        this.languageService = languageService;
        this.languageAssembler = languageAssembler;
        this.statisticsService = statisticsService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/languages")
    public List<LanguageDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Language> all = languageService.findAll();
        languageService.sortBySize(all);
        return languageAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.POST, value = "api/language")
    public ResponseEntity<Object> create(@RequestBody LanguageDTO languageDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Language language = languageAssembler.createModel(languageDTO);
        final Language savedLanguage = languageService.save(language);
        if (savedLanguage != null) {
            return new ResponseEntity<>(savedLanguage, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
