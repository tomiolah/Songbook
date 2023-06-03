package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;

import java.util.Date;

@SuppressWarnings("unused")
public class ProjectorVersionUtil {
    public static void createNewProjectorVersion(ProjectorVersionRepository projectorVersionRepository) {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("3.2.4");
        projectorVersion.setCreatedDate(new Date());
        projectorVersion.setDescription("Ability to update bibles");
        projectorVersion.setVersionId(57);
        projectorVersionRepository.save(projectorVersion);
    }
}
