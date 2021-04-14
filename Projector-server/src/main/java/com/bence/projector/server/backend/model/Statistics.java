package com.bence.projector.server.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.util.Date;

@Entity
public class Statistics extends BaseEntity {
    private Date accessedDate;
    @Column(length = 15)
    private String remoteAddress;
    @Column(length = 100)
    private String uri;
    @Column(length = 10)
    private String method;

    public Date getAccessedDate() {
        return accessedDate == null ? null : (Date) accessedDate.clone();
    }

    public void setAccessedDate(Date accessedDate) {
        this.accessedDate = accessedDate == null ? null : (Date) accessedDate.clone();
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
