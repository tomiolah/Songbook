package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Entity
public class UserProperties extends BaseEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userProperties")
    private List<NotificationByLanguage> notifications;

    public List<NotificationByLanguage> getNotifications() {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        return notifications;
    }

    @Transactional
    public void setNotifications(List<NotificationByLanguage> notifications) {
        if (notifications != null) {
            for (NotificationByLanguage notificationByLanguage : notifications) {
                notificationByLanguage.setUserProperties(this);
            }
            if (this.notifications != null) {
                this.notifications.clear();
                this.notifications.addAll(notifications);
            } else {
                this.notifications = notifications;
            }
        } else {
            this.notifications = null;
        }
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
