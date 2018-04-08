package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.projector.server.api.assembler.SuggestionAssembler;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.SuggestionService;
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
public class SuggestionResource {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private SuggestionService suggestionService;
    @Autowired
    private SuggestionAssembler suggestionAssembler;
    @Autowired
    private FreemarkerConfiguration freemarkerConfiguration;
    @Qualifier("javaMailSender")
    @Autowired
    private JavaMailSender sender;

    @RequestMapping(value = "admin/api/suggestions", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestions() {
        List<Suggestion> all = suggestionService.findAll();
        return suggestionAssembler.createDtoList(all);
    }

    @RequestMapping(value = "admin/api/suggestion/{id}", method = RequestMethod.GET)
    public SuggestionDTO getSuggestion(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Suggestion suggestion = suggestionService.findOne(id);
        return suggestionAssembler.createDto(suggestion);
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
        final String freemarkerName = FreemarkerConfiguration.NEW_SUGGESTION + ".ftl";
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
        helper.setSubject("Új javaslat");

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
                    "    <h3>Új javaslat: " + title + "</h3>\n" +
                    "    <a href=\"https://projector-songbook.herokuapp.com/#/suggestion/" + suggestion.getId() + "\">Link</a>\n" +
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
        data.put("title", title);
        data.put("songUuid", suggestion.getId());
        data.put("email", createdByEmail);
        data.put("description", description);
        return data;
    }
}
