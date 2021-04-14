package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class UserProperties extends BaseEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userProperties")
    private List<NotificationByLanguage> notifications;

    public List<NotificationByLanguage> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationByLanguage> notifications) {
        this.notifications = notifications;
    }

    public NotificationByLanguage getNotificationByLanguage(Language language) {
        for (NotificationByLanguage notification : getNotifications()) {
            if (notification.getLanguage().getUuid().equals(language.getUuid())) {
                return notification;
            }
        }
        return null;
    }
}
