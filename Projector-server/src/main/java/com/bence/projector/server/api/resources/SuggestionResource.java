package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.projector.server.api.assembler.SuggestionAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.SuggestionService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bence.projector.server.api.resources.SongResource.songInReviewLanguages;
import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SuggestionResource {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private SuggestionService suggestionService;
    @Autowired
    private SuggestionAssembler suggestionAssembler;
    @Autowired
    private LanguageService languageService;
    @Qualifier("javaMailSender")
    @Autowired
    private JavaMailSender sender;
    @Autowired
    private UserService userService;
    @Autowired
    private SongService songService;

    @RequestMapping(value = "admin/api/suggestions", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestions() {
        List<Suggestion> all = suggestionService.findAll();
        return suggestionAssembler.createDtoList(all);
    }

    @RequestMapping(value = "reviewer/api/suggestions", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestionsR() {
        return getSuggestions();
    }

    private List<SuggestionDTO> getSuggestionDTOS(@PathVariable("languageId") String languageId) {
        Language language = languageService.findOne(languageId);
        List<Suggestion> allByLanguage = suggestionService.findAllByLanguage(language);
        return suggestionAssembler.createDtoList(allByLanguage);
    }

    @RequestMapping(value = "admin/api/suggestions/language/{languageId}", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestionsByLanguage(@PathVariable("languageId") String languageId) {
        return getSuggestionDTOS(languageId);
    }

    @RequestMapping(value = "reviewer/api/suggestions/language/{languageId}", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestionsByLanguageR(@PathVariable("languageId") String languageId) {
        return getSuggestionDTOS(languageId);
    }

    private SuggestionDTO getSuggestionDTO(@PathVariable String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Suggestion suggestion = suggestionService.findOne(id);
        return suggestionAssembler.createDto(suggestion);
    }

    @RequestMapping(value = "admin/api/suggestion/{id}", method = RequestMethod.GET)
    public SuggestionDTO getSuggestion(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        return getSuggestionDTO(id, httpServletRequest);
    }

    @RequestMapping(value = "reviewer/api/suggestion/{id}", method = RequestMethod.GET)
    public SuggestionDTO getSuggestionR(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        return getSuggestionDTO(id, httpServletRequest);
    }

    @RequestMapping(value = "api/suggestion", method = RequestMethod.POST)
    public SuggestionDTO suggestion(@RequestBody final SuggestionDTO suggestionDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Suggestion model = suggestionAssembler.createModel(suggestionDTO);
        if (model != null) {
            Suggestion suggestion = suggestionService.save(model);
            Thread thread = new Thread(() -> {
                try {
                    sendEmail(suggestion);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        return suggestionAssembler.createDto(model);
    }

    private void sendEmail(Suggestion suggestion)
            throws MessagingException, MailSendException {
        Song song = songService.findOne(suggestion.getSongId());
        List<User> reviewers = userService.findAllReviewersByLanguage(song.getLanguage());
        for (User user : reviewers) {
            NotificationByLanguage notificationByLanguage = user.getUserProperties().getNotificationByLanguage(song.getLanguage());
            if (notificationByLanguage != null && notificationByLanguage.isSuggestions()) {
                sendEmailToUser(suggestion, user);
            }
        }
    }

    private void sendEmailToUser(Suggestion suggestion, User user)
            throws MessagingException, MailSendException {
        final String freemarkerName = FreemarkerConfiguration.NEW_SUGGESTION + ".ftl";
        freemarker.template.Configuration config = ConfigurationUtil.getConfiguration();
        config.setDefaultEncoding("UTF-8");
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(new InternetAddress(user.getEmail()));
        helper.setFrom(new InternetAddress("noreply@songbook"));
        helper.setSubject("New suggestion");

        try {
            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            template.process(createPattern(suggestion), writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            String title = suggestion.getTitle();
            if (title == null) {
                title = "";
            }
            String description = suggestion.getDescription();
            if (description == null) {
                description = "";
            }
            String createdByEmail = suggestion.getCreatedByEmail();
            if (createdByEmail == null) {
                createdByEmail = "";
            }
            helper.getMimeMessage().setContent("<div>\n" +
                    "    <h3>New suggestion: " + title + "</h3>\n" +
                    "    <a href=\"" + AppProperties.getInstance().baseUrl() + "/#/suggestion/" + suggestion.getId() + "\">Link</a>\n" +
                    "  <h3>Email </h3><h4>" + createdByEmail + "</h4>" +
                    "  <h3>" + description + "</h3>" +
                    "</div>", "text/html;charset=utf-8");
        }
        sender.send(message);
    }

    private Map<String, Object> createPattern(Suggestion suggestion) {
        Map<String, Object> data = new HashMap<>();
        String title = suggestion.getTitle();
        String createdByEmail = suggestion.getCreatedByEmail();
        String description = suggestion.getDescription();
        if (title == null) {
            title = "";
        }
        if (description == null) {
            description = "";
        }
        if (createdByEmail == null) {
            createdByEmail = "";
        }
        data.put("baseUrl", AppProperties.getInstance().baseUrl());
        data.put("title", title);
        data.put("id", suggestion.getId());
        data.put("email", createdByEmail);
        data.put("description", description);
        return data;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/api/suggestion/{suggestionId}")
    public ResponseEntity<Object> updateSongByAdmin(Principal principal, @PathVariable final String suggestionId, @RequestBody final SuggestionDTO suggestionDTO, HttpServletRequest httpServletRequest) {
        return updateSongByReviewer(principal, suggestionId, suggestionDTO, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reviewer/api/suggestion/{suggestionId}")
    public ResponseEntity<Object> updateSongByReviewer(Principal principal, @PathVariable final String suggestionId, @RequestBody final SuggestionDTO suggestionDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                Suggestion suggestion = suggestionService.findOne(suggestionId);
                if (suggestion != null) {
                    Song song = songService.findOne(suggestion.getSongId());
                    if (song != null && songInReviewLanguages(user, song)) {
                        Date modifiedDate = suggestion.getModifiedDate();
                        if ((modifiedDate != null && modifiedDate.compareTo(suggestionDTO.getModifiedDate()) != 0) || (modifiedDate == null && suggestionDTO.getModifiedDate() != null)) {
                            return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
                        }
                        suggestionAssembler.updateModel(suggestion, suggestionDTO);
                        suggestion.setModifiedDate(new Date());
                        suggestion.setLastModifiedBy(user);
                        final Suggestion savedSuggestion = suggestionService.save(suggestion);
                        if (savedSuggestion != null) {
                            return new ResponseEntity<>(suggestionAssembler.createDto(savedSuggestion), HttpStatus.ACCEPTED);
                        }
                    }
                }
                return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }
}
