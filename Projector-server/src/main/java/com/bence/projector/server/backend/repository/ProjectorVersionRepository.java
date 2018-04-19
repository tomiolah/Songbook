package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.ProjectorVersion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectorVersionRepository extends MongoRepository<ProjectorVersion, String> {

    List<ProjectorVersion> findAllByVersionIdGreaterThan(int nr);
}
