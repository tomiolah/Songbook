package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.UserProperties;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserPropertiesRepository extends MongoRepository<UserProperties, String> {
}
