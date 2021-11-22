package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;

import java.util.Date;

public class ProjectorVersionUtil {
    public static void createNewProjectorVersion(ProjectorVersionRepository projectorVersionRepository) {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("2.5.0.1");
        projectorVersion.setCreatedDate(new Date());
        projectorVersion.setDescription("Multiple song collection fix.");
        projectorVersion.setVersionId(20);
        projectorVersionRepository.save(projectorVersion);
    }
}
