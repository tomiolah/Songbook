package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import com.bence.projector.server.api.assembler.SongCollectionAssembler;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongCollectionResource {

    private final SongCollectionService songCollectionService;
    private final SongCollectionAssembler songCollectionAssembler;
    private final StatisticsService statisticsService;

    @Autowired
    public SongCollectionResource(SongCollectionService songCollectionService, SongCollectionAssembler songCollectionAssembler, StatisticsService statisticsService) {
        this.songCollectionService = songCollectionService;
        this.songCollectionAssembler = songCollectionAssembler;
        this.statisticsService = statisticsService;
    }

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

    @RequestMapping(method = RequestMethod.PUT, value = "api/songCollection/{songCollectionUuid}/songCollectionElement")
    public ResponseEntity<Object> addToSongCollection(HttpServletRequest httpServletRequest, @PathVariable String songCollectionUuid, @RequestBody SongCollectionElementDTO elementDTO) {
        saveStatistics(httpServletRequest, statisticsService);
        final SongCollection songCollection = songCollectionService.findOne(songCollectionUuid);
        if (songCollection != null) {
            List<SongCollectionElement> songCollectionElements = songCollection.getSongCollectionElements();
            SongCollectionElement elementModel = null;
            for (SongCollectionElement element : songCollectionElements) {
                if (element.getSongUuid().equals(elementDTO.getSongUuid())) {
                    elementModel = element;
                    break;
                }
            }
            if (elementModel == null) {
                elementModel = songCollectionAssembler.createElementModel(elementDTO);
                songCollectionElements.add(elementModel);
            } else {
                elementModel.setOrdinalNumber(elementDTO.getOrdinalNumber());
            }
            songCollection.setModifiedDate(new Date());
            songCollectionService.save(songCollection);
            return new ResponseEntity<>(songCollectionAssembler.createDto(songCollection), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
