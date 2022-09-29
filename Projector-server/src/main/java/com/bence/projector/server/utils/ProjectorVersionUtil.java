package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;

import java.util.Date;

public class ProjectorVersionUtil {
    public static void createNewProjectorVersion(ProjectorVersionRepository projectorVersionRepository) {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("3.0.2");
        projectorVersion.setCreatedDate(new Date());
        projectorVersion.setDescription("Update test");
        projectorVersion.setVersionId(40);
        projectorVersionRepository.save(projectorVersion);
    }
}
