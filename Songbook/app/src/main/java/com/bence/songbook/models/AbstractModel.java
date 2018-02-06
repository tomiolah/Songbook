package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.util.UUID;

public abstract class AbstractModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @DatabaseField(index = true)
    private String uuid;

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((this.getUuid() == null) ? 0 : this.getUuid().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractModel that = (AbstractModel) o;

        return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;
    }

    public String getUuid() {
        ensureUuid();
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    private void ensureUuid() {
        if (uuid == null) {
            setUuid(UUID.randomUUID().toString());
        }
    }
}
