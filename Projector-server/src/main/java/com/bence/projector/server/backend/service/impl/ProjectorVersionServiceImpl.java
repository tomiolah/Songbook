package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;
import com.bence.projector.server.backend.service.ProjectorVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectorVersionServiceImpl extends BaseServiceImpl<ProjectorVersion> implements ProjectorVersionService {
    @Autowired
    private ProjectorVersionRepository projectorVersionRepository;

    @Override
    public List<ProjectorVersion> findAllAfterCreatedNr(int nr) {
        return projectorVersionRepository.findAllByVersionIdGreaterThan(nr);
    }

    @Override
    public ProjectorVersion findOneByUuid(String uuid) {
        return null;
    }

    @Override
    public void deleteByUuid(String uuid) {

    }
}
