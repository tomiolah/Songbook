package com.bence.projector.server.backend.model;

import java.util.List;

public class UserProperties extends BaseEntity {

    private List<NotificationByLanguage> notifications;

    public List<NotificationByLanguage> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationByLanguage> notifications) {
        this.notifications = notifications;
    }

    public NotificationByLanguage getNotificationByLanguage(Language language) {
        for (NotificationByLanguage notification: getNotifications()) {
            if (notification.getLanguage().getId().equals(language.getId())) {
                return notification;
            }
        }
        return null;
    }
}
