package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;

import java.util.Date;

@SuppressWarnings("unused")
public class ProjectorVersionUtil {
    public static void createNewProjectorVersion(ProjectorVersionRepository projectorVersionRepository) {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("3.0.5");
        projectorVersion.setCreatedDate(new Date());
        projectorVersion.setDescription("Bible verse selections fix");
        projectorVersion.setVersionId(43);
        projectorVersionRepository.save(projectorVersion);
    }
}
