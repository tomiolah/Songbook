package com.bence.projector.server.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

public class BaseEntity {

    @Id
    @Indexed
    private String id;

    public BaseEntity() {
        id = null;
    }

    public BaseEntity(final String id) {
        this.id = id;
    }

    public BaseEntity(BaseEntity baseEntity) {
        this.id = baseEntity.id;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public boolean isSameId(BaseEntity baseEntity) {
        return id != null && baseEntity != null && baseEntity.id != null && id.equals(baseEntity.id);
    }
}
