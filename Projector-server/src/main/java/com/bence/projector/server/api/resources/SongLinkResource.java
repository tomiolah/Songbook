package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongLinkDTO;
import com.bence.projector.server.api.assembler.SongLinkAssembler;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.service.SongLinkService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.mailsending.FreemarkerConfiguration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongLinkResource {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private SongLinkService songLinkService;
    @Autowired
    private SongLinkAssembler songLinkAssembler;
    @Autowired
    private FreemarkerConfiguration freemarkerConfiguration;
    @Qualifier("javaMailSender")
    @Autowired
    private JavaMailSender sender;

    @RequestMapping(value = "admin/api/songLinks", method = RequestMethod.GET)
    public List<SongLinkDTO> getSongLinks() {
        List<SongLink> all = songLinkService.findAll();
        return songLinkAssembler.createDtoList(all);
    }

    @RequestMapping(value = "admin/api/songLink/{id}", method = RequestMethod.GET)
    public SongLinkDTO getSongLink(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongLink songLink = songLinkService.findOne(id);
        return songLinkAssembler.createDto(songLink);
    }

    @RequestMapping(value = "api/songLink", method = RequestMethod.POST)
    public SongLinkDTO songLink(@RequestBody final SongLinkDTO songLinkDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongLink model = songLinkAssembler.createModel(songLinkDTO);
        if (model != null) {
            SongLink songLink = songLinkService.save(model);
            Thread thread = new Thread(() -> {
                try {
                    sendEmail(songLink);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        return songLinkAssembler.createDto(model);
    }

    private void sendEmail(SongLink songLink)
            throws MessagingException, MailSendException {
        final String freemarkerName = FreemarkerConfiguration.NEW_SONG_LINK + ".ftl";
        FreeMarkerConfigurer freemarkerConfigurer = freemarkerConfiguration.freemarkerConfig();
        freemarker.template.Configuration config = freemarkerConfigurer.getConfiguration();
        config.setDefaultEncoding("UTF-8");
        try {
            config.setDirectoryForTemplateLoading(new File(freemarkerConfiguration.findParent(freemarkerName)));
        } catch (Exception e) {
            e.printStackTrace();
            config.setClassForTemplateLoading(this.getClass(), "/");
        }
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(new InternetAddress("bakobence@yahoo.com"));
        helper.setFrom(new InternetAddress("noreply@songbook"));
        helper.setSubject("Új verzió összekötés");

        try {
            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            template.process(createPattern(songLink), writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            String createdByEmail = songLink.getCreatedByEmail();
            if (createdByEmail == null) {
                createdByEmail = "";
            }
            helper.getMimeMessage().setContent("<div>\n" +
                    "    <h3>Új verzió összekötés: </h3>\n" +
                    "    <a href=\"https://projector-songbook.herokuapp.com/#/song/" + songLink.getSongId1() + "\">Link1</a>\n" +
                    "<br><a href=\"https://projector-songbook.herokuapp.com/#/song/" + songLink.getSongId2() + "\">Link2</a>\n" +
                    "  <h3>Email </h3><h4>" + createdByEmail + "</h4>" +
                    "</div>", "text/html;charset=utf-8");
        }
        sender.send(message);
    }

    private Map<String, Object> createPattern(SongLink songLink) {
        Map<String, Object> data = new HashMap<>();
        String createdByEmail = songLink.getCreatedByEmail();
        if (createdByEmail == null) {
            createdByEmail = "";
        }
        data.put("id", songLink.getId());
        data.put("email", createdByEmail);
        data.put("song1", songLink.getSongId1());
        data.put("song2", songLink.getSongId2());
        return data;
    }
}
