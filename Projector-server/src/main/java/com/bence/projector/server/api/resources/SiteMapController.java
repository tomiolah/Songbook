package com.bence.projector.server.api.resources;

import com.bence.projector.server.api.siteMap.XmlUrl;
import com.bence.projector.server.api.siteMap.XmlUrlSet;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

@Controller
public class SiteMapController {
    @Autowired
    private SongService songService;

    @RequestMapping(value = "/sitemap.xml", method = RequestMethod.GET)
    @ResponseBody
    public XmlUrlSet main() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();
        List<Song> all = songService.findAll();
        for (Song song : all) {
            if (!song.isDeleted()) {
                create(xmlUrlSet, song.getId(), song.getModifiedDate());
            }
        }
        return xmlUrlSet;
    }

    private void create(XmlUrlSet xmlUrlSet, String uuid, Date date) {
        xmlUrlSet.addUrl(new XmlUrl("https://projector-songbook.herokuapp.com/song/" + uuid, date));
    }
}