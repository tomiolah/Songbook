package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.NotificationByLanguage;
import org.springframework.data.repository.CrudRepository;

public interface NotificationByLanguageRepository extends CrudRepository<NotificationByLanguage, Long> {
}
