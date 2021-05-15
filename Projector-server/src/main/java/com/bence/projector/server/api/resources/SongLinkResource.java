package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongLinkDTO;
import com.bence.projector.server.api.assembler.SongLinkAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongLinkService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.ConfigurationUtil;
import com.bence.projector.server.mailsending.FreemarkerConfiguration;
import com.bence.projector.server.utils.AppProperties;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongLinkResource {
    private final StatisticsService statisticsService;
    private final SongLinkService songLinkService;
    private final SongLinkAssembler songLinkAssembler;
    private final JavaMailSender sender;
    private final UserService userService;
    private final SongService songService;
    private final LanguageService languageService;

    @Autowired
    public SongLinkResource(StatisticsService statisticsService, SongLinkService songLinkService, SongLinkAssembler songLinkAssembler, @Qualifier("javaMailSender") JavaMailSender sender, UserService userService, SongService songService, LanguageService languageService) {
        this.statisticsService = statisticsService;
        this.songLinkService = songLinkService;
        this.songLinkAssembler = songLinkAssembler;
        this.sender = sender;
        this.userService = userService;
        this.songService = songService;
        this.languageService = languageService;
    }

    @RequestMapping(value = "admin/api/songLinks", method = RequestMethod.GET)
    public List<SongLinkDTO> getSongLinks() {
        List<SongLink> all = songLinkService.findAllUnApplied();
        return songLinkAssembler.createDtoList(all);
    }

    @RequestMapping(value = "admin/api/songLinks/resolveApplied", method = RequestMethod.GET)
    public List<SongLinkDTO> resolveAppliedSongLinks() {
        songLinkService.resolveAppliedSongLinks();
        return songLinkAssembler.createDtoList(new ArrayList<>());
    }

    @RequestMapping(value = "admin/api/songLinks/language/{languageId}", method = RequestMethod.GET)
    public List<SongLinkDTO> getSongLinksByLanguage(@PathVariable("languageId") String languageId) {
        Language language = languageService.findOneByUuid(languageId);
        List<SongLink> songLinks = songLinkService.findAllByLanguage(language);
        return songLinkAssembler.createDtoList(songLinks);
    }

    @RequestMapping(value = "admin/api/songLink/{id}", method = RequestMethod.GET)
    public SongLinkDTO getSongLink(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongLink songLink = songLinkService.findOneByUuid(id);
        return songLinkAssembler.createDto(songLink);
    }

    @RequestMapping(value = "api/songLink", method = RequestMethod.POST)
    public SongLinkDTO songLink(@RequestBody final SongLinkDTO songLinkDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongLink model = songLinkAssembler.createModel(songLinkDTO);
        if (model != null && !model.alreadyTheSameVersionGroup(songService)) {
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

    @RequestMapping(value = "admin/api/songLink/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Object> updateSongLink(@PathVariable final String id,
                                                 @RequestBody final SongLinkDTO songLinkDTO,
                                                 Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                SongLink songLink = songLinkService.findOneByUuid(id);
                if (songLink != null) {
                    Date modifiedDate = songLink.getModifiedDate();
                    if (modifiedDate != null && modifiedDate.compareTo(songLinkDTO.getModifiedDate()) != 0) {
                        return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
                    }
                    songLinkAssembler.updateModel(songLink, songLinkDTO);
                    songLink.setModifiedDate(new Date());
                    songLinkService.save(songLink);
                    return new ResponseEntity<>(songLinkAssembler.createDto(songLink), HttpStatus.ACCEPTED);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(value = "user/api/songVersionGroup/{songId1}/{songId2}", method = RequestMethod.POST)
    public ResponseEntity<Object> userSongLink(@PathVariable("songId1") String songId1, @PathVariable("songId2") String songId2, HttpServletRequest httpServletRequest, Principal principal) {
        if (songId1.equals(songId2)) {
            return new ResponseEntity<>("Same song", HttpStatus.CONFLICT);
        }
        Song song1 = songService.findOneByUuid(songId1);
        Song song2 = songService.findOneByUuid(songId2);
        if (song1 == null || song2 == null || song1.isSameVersionGroup(song2)) {
            return new ResponseEntity<>("Null", HttpStatus.NO_CONTENT);
        }
        saveStatistics(httpServletRequest, statisticsService);
        User user = null;
        if (principal != null) {
            String email = principal.getName();
            user = userService.findByEmail(email);
        }
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        SongLink model = new SongLink();
        model.setApplied(false);
        model.setCreatedDate(new Date());
        model.setSong1(songService.findOneByUuid(songId1));
        model.setSong2(songService.findOneByUuid(songId2));
        model.setCreatedByEmail(user.getEmail());
        SongLink songLink = songLinkService.save(model);
        Thread thread = new Thread(() -> {
            try {
                sendEmail(songLink);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        return new ResponseEntity<>(songLinkAssembler.createDto(model), HttpStatus.ACCEPTED);
    }

    private void sendEmail(SongLink songLink)
            throws MessagingException, MailSendException {
        final String freemarkerName = FreemarkerConfiguration.NEW_SONG_LINK + ".ftl";
        freemarker.template.Configuration config = ConfigurationUtil.getConfiguration();
        config.setDefaultEncoding("UTF-8");
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(new InternetAddress("name@domain.com"));
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
                    "    <a href=\"" + AppProperties.getInstance().baseUrl() + "/#/song/" + songLink.getSong1(songService).getUuid() + "\">Link1</a>\n" +
                    "<br><a href=\"" + AppProperties.getInstance().baseUrl() + "/#/song/" + songLink.getSong2(songService).getUuid() + "\">Link2</a>\n" +
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
        data.put("baseUrl", AppProperties.getInstance().baseUrl());
        data.put("id", songLink.getUuid());
        data.put("email", createdByEmail);
        Song song1 = songLink.getSong1(songService);
        Song song2 = songLink.getSong2(songService);
        data.put("song1Title", song1.getTitle());
        data.put("song2Title", song2.getTitle());
        data.put("song1", song1.getUuid());
        data.put("song2", song2.getUuid());
        return data;
    }
}
