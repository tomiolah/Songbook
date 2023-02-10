package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;

import java.util.Date;

@SuppressWarnings("unused")
public class ProjectorVersionUtil {
    public static void createNewProjectorVersion(ProjectorVersionRepository projectorVersionRepository) {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("3.2.2");
        projectorVersion.setCreatedDate(new Date());
        projectorVersion.setDescription("Default system window decorations is replaced by a custom title bar. Better dark theme applied.");
        projectorVersion.setVersionId(55);
        projectorVersionRepository.save(projectorVersion);
    }
}
