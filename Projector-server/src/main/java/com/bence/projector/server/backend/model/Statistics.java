package com.bence.projector.server.backend.model;

import java.util.Date;

public class Statistics extends BaseEntity {
    private Date accessedDate;
    private String remoteAddress;
    private String uri;
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
