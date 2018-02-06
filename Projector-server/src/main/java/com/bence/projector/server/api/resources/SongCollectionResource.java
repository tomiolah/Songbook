package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.server.api.assembler.SongCollectionAssembler;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongCollectionResource {

    @Autowired
    private SongCollectionService songCollectionService;
    @Autowired
    private SongCollectionAssembler songCollectionAssembler;
    @Autowired
    private StatisticsService statisticsService;

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections")
    public List<SongCollectionDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<SongCollection> all = songCollectionService.findAll();
        return songCollectionAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections/language/{languageId}/lastModifiedDate/{lastModifiedDate}")
    public List<SongCollectionDTO> findAllByLanguage(@PathVariable("languageId") String languageId, @PathVariable("lastModifiedDate") Long lastModifiedDate, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<SongCollection> all = songCollectionService.findAllByLanguage_IdAndAndModifiedDateGreaterThan(languageId, new Date(lastModifiedDate));
        return songCollectionAssembler.createDtoList(all);
    }
}
