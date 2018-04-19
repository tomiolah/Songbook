package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import com.bence.projector.server.api.assembler.ProjectorVersionAssembler;
import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.service.ProjectorVersionService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class ProjectorVersionResource {

    @Autowired
    private ProjectorVersionService projectorVersionService;
    @Autowired
    private ProjectorVersionAssembler projectorVersionAssembler;
    @Autowired
    private StatisticsService statisticsService;

    @RequestMapping(method = RequestMethod.GET, value = "/api/projectorVersionsAfterNr/{nr}")
    public List<ProjectorVersionDTO> findAllAfterDate(HttpServletRequest httpServletRequest, @PathVariable("nr") int nr) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<ProjectorVersion> projectorVersions = projectorVersionService.findAllAfterCreatedNr(nr);
        return projectorVersionAssembler.createDtoList(projectorVersions);
    }
}
