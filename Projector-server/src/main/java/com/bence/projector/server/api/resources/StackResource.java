package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.StackDTO;
import com.bence.projector.server.api.assembler.StackAssembler;
import com.bence.projector.server.backend.model.Stack;
import com.bence.projector.server.backend.service.StackService;
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
public class StackResource {
    private final StatisticsService statisticsService;
    private final StackService stackService;
    private final StackAssembler stackAssembler;
    private final FreemarkerConfiguration freemarkerConfiguration;
    private final JavaMailSender sender;

    @Autowired
    public StackResource(StatisticsService statisticsService, StackService stackService, StackAssembler stackAssembler, FreemarkerConfiguration freemarkerConfiguration, @Qualifier("javaMailSender") JavaMailSender sender) {
        this.statisticsService = statisticsService;
        this.stackService = stackService;
        this.stackAssembler = stackAssembler;
        this.freemarkerConfiguration = freemarkerConfiguration;
        this.sender = sender;
    }

    @RequestMapping(value = "admin/api/stacks", method = RequestMethod.GET)
    public List<StackDTO> getStacks() {
        List<Stack> all = stackService.findAll();
        return stackAssembler.createDtoList(all);
    }

    @RequestMapping(value = "admin/api/stack/{id}", method = RequestMethod.GET)
    public StackDTO getStack(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Stack stack = stackService.findOne(id);
        return stackAssembler.createDto(stack);
    }

    @RequestMapping(value = "api/stack", method = RequestMethod.POST)
    public StackDTO stack(@RequestBody final StackDTO stackDTO) {
        Stack model = stackAssembler.createModel(stackDTO);
        if (model != null) {
            Stack byStackTrace = stackService.findByStackTrace(model.getStackTrace());
            if (byStackTrace != null) {
                byStackTrace.setCount(byStackTrace.getCount() + 1);
                stackService.save(byStackTrace);
                return stackAssembler.createDto(byStackTrace);
            }
            model.setCount(1);
            Stack stack = stackService.save(model);
            Thread thread = new Thread(() -> {
                try {
                    sendEmail(stack);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }
        return stackAssembler.createDto(model);
    }

    private void sendEmail(Stack stack)
            throws MessagingException, MailSendException {
        final String freemarkerName = FreemarkerConfiguration.NEW_STACK + ".ftl";
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
        helper.setSubject("Stack trace");

        try {
            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            template.process(createPattern(stack), writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        } catch (Exception ignored) {
        }
        sender.send(message);
    }

    private Map<String, Object> createPattern(Stack stack) {
        Map<String, Object> data = new HashMap<>();
        String createdByEmail = stack.getEmail();
        if (createdByEmail == null) {
            createdByEmail = "";
        }
        data.put("id", stack.getId());
        data.put("email", createdByEmail);
        data.put("message", stack.getMessage());
        data.put("stackTrace", stack.getStackTrace());
        data.put("version", stack.getVersion());
        return data;
    }
}