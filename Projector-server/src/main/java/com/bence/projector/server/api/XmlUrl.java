package com.bence.projector.server.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("FieldCanBeLocal")
@XmlAccessorType(value = XmlAccessType.NONE)
@XmlRootElement(name = "url")
public class XmlUrl {
    @XmlElement
    private String loc;
    @XmlElement
    private String lastmod;

    public XmlUrl() {
    }

    public XmlUrl(String loc, Date date) {
        this.loc = loc;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        lastmod = simpleDateFormat.format(date);
    }

}