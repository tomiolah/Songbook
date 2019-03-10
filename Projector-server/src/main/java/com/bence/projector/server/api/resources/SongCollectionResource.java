package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import com.bence.projector.server.api.assembler.SongCollectionAssembler;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.mailsending.FreemarkerConfiguration;
import com.bence.projector.server.utils.AppProperties;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongCollectionResource {

    private final SongCollectionService songCollectionService;
    private final SongCollectionAssembler songCollectionAssembler;
    private final StatisticsService statisticsService;
    private final FreemarkerConfiguration freemarkerConfiguration;
    private final JavaMailSender sender;

    @Autowired
    public SongCollectionResource(SongCollectionService songCollectionService, SongCollectionAssembler songCollectionAssembler, StatisticsService statisticsService, FreemarkerConfiguration freemarkerConfiguration, JavaMailSender sender) {
        this.songCollectionService = songCollectionService;
        this.songCollectionAssembler = songCollectionAssembler;
        this.statisticsService = statisticsService;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.sender = sender;
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections")
    public List<SongCollectionDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<SongCollection> all = songCollectionService.findAll();
        return songCollectionAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songCollection/{id}")
    public SongCollectionDTO find(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final SongCollection songCollectionDTO = songCollectionService.findOne(id);
        return songCollectionAssembler.createDto(songCollectionDTO);
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections/language/{languageId}/lastModifiedDate/{lastModifiedDate}")
    public List<SongCollectionDTO> findAllByLanguage(@PathVariable("languageId") String languageId, @PathVariable("lastModifiedDate") Long lastModifiedDate, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<SongCollection> all = songCollectionService.findAllByLanguage_IdAndAndModifiedDateGreaterThan(languageId, new Date(lastModifiedDate));
        return songCollectionAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "admin/api/songCollection/{songCollectionUuid}/songCollectionElement")
    public ResponseEntity<Object> addToSongCollection(HttpServletRequest httpServletRequest, @PathVariable String songCollectionUuid, @RequestBody SongCollectionElementDTO elementDTO) {
        saveStatistics(httpServletRequest, statisticsService);
        final SongCollection songCollection = songCollectionService.findOne(songCollectionUuid);
        if (songCollection != null) {
            List<SongCollectionElement> songCollectionElements = songCollection.getSongCollectionElements();
            SongCollectionElement elementModel = null;
            for (SongCollectionElement element : songCollectionElements) {
                if (element.getSongUuid().equals(elementDTO.getSongUuid())) {
                    elementModel = element;
                    break;
                }
            }
            if (elementModel == null) {
                elementModel = songCollectionAssembler.createElementModel(elementDTO);
                songCollectionElements.add(elementModel);
            } else {
                elementModel.setOrdinalNumber(elementDTO.getOrdinalNumber());
            }
            songCollection.setModifiedDate(new Date());
            songCollectionService.save(songCollection);
            return new ResponseEntity<>(songCollectionAssembler.createDto(songCollection), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/songCollection/upload")
    public ResponseEntity<Object> uploadSong(@RequestBody final SongCollectionDTO songCollectionDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final SongCollection songCollection = songCollectionAssembler.createModel(songCollectionDTO);
        songCollection.setOriginalId(songCollectionDTO.getUuid());
        songCollection.setDeleted(true);
        songCollection.setUploaded(true);
        final SongCollection savedSongCollection = songCollectionService.save(songCollection);
        if (savedSongCollection != null) {
            Thread thread = new Thread(() -> {
                try {
                    List<SongCollection> songCollections = songCollectionService.findAll();
                    boolean deleted = false;
                    for (SongCollection songCollection1 : songCollections) {
                        if (!savedSongCollection.getId().equals(songCollection.getId()) && songCollectionService.matches(savedSongCollection, songCollection1)) {
                            songCollectionService.delete(savedSongCollection.getId());
                            deleted = true;
                            break;
                        }
                    }
                    if (!deleted) {
                        sendEmail(savedSongCollection);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            SongCollectionDTO dto = songCollectionAssembler.createDto(savedSongCollection);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendEmail(SongCollection song)
            throws MessagingException, MailSendException {
        final String freemarkerName = FreemarkerConfiguration.COLLECTION_UPDATE + ".ftl";
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
        helper.setSubject("Gyűjtemény frissítése");
        try {
            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            template.process(createPattern(song), writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            helper.getMimeMessage().setContent("<div>\n" +
                    "    <h3>Gyűjtemény frissítése: " + song.getName() + "</h3>\n" +
                    "    <a href=\"" + AppProperties.getInstance().baseUrl() + "/api/songCollection/" + song.getId() + "\">Link</a>\n" +
                    "</div>", "text/html;charset=utf-8");
        }
        sender.send(message);
    }

    private Map<String, Object> createPattern(SongCollection songCollection) {
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", AppProperties.getInstance().baseUrl());
        data.put("name", songCollection.getName());
        data.put("uuid", songCollection.getId());
        return data;
    }
}
