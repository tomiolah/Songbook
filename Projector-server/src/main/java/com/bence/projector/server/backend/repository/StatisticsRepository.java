package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Statistics;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StatisticsRepository extends MongoRepository<Statistics, String> {
}
