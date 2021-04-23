package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.UserProperties;
import com.bence.projector.server.backend.service.NotificationByLanguageService;
import com.bence.projector.server.backend.service.UserPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPropertiesServiceImpl extends BaseServiceImpl<UserProperties> implements UserPropertiesService {

    @Autowired
    private NotificationByLanguageService notificationByLanguageService;

    @Override
    public UserProperties save(UserProperties userProperties) {
        UserProperties save = super.save(userProperties);
        notificationByLanguageService.deleteAllByUserProperties(userProperties);
        notificationByLanguageService.save(userProperties.getNotifications());
        return save;
    }

    @Override
    public Iterable<UserProperties> save(List<UserProperties> models) {
        for (UserProperties userProperty : models) {
            save(userProperty);
        }
        return models;
    }
}
