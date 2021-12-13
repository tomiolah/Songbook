package com.bence.projector.server.mailsending;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.NotificationByLanguageService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.utils.AppProperties;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bence.projector.server.api.resources.PasswordController.TOKEN_DURATION;

@Component
public class MailSenderService {
    @Qualifier("javaMailSender")
    @Autowired
    private JavaMailSender sender;
    @Autowired
    private SongService songService;
    @Autowired
    private UserService userService;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private NotificationByLanguageService notificationByLanguageService;

    public void sendEmailSuggestionToUser(Suggestion suggestion, User user) {
        try {
            Language language = songService.findOneByUuid(suggestion.getSongUuid()).getLanguage();
            NotificationByLanguage notificationByLanguage = user.getNotificationByLanguage(language);
            notificationByLanguage.getSuggestionStack().add(suggestion);
            notificationByLanguageService.save(notificationByLanguage);
            tryToSendAllPrevious();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(user.getEmail());
            System.out.println(suggestion.getTitle());
        }
    }

    public void sendEmailNewSongToUser(Song song, User user) {
        Language language = song.getLanguage();
        NotificationByLanguage notificationByLanguage = user.getNotificationByLanguage(language);
        notificationByLanguage.getNewSongStack().add(song);
        notificationByLanguageService.save(notificationByLanguage);
        tryToSendAllPrevious();
    }

    public void tryToSendAllPrevious() {
        List<User> reviewers = userService.findAllReviewers();
        for (User reviewer : reviewers) {
            List<Language> languages;
            if (reviewer.getRole() == Role.ROLE_ADMIN) {
                languages = languageService.findAll();
            } else {
                languages = reviewer.getReviewLanguages();
            }
            for (Language language : languages) {
                tryToSend(language, reviewer);
            }
        }
    }

    private void tryToSend(Language language, User user) {
        user = userService.findOneByUuid(user.getUuid());
        NotificationByLanguage notificationByLanguage = user.getNotificationByLanguage(language);
        List<Suggestion> suggestionStack = notificationByLanguage.getSuggestionStack();
        int suggestionStackSize = suggestionStack.size();
        if (suggestionStackSize > 0 && notificationByLanguage.isSuggestions()) {
            Date now = new Date();
            if (suggestionStackSize > 49 || now.getTime() - notificationByLanguage.getSuggestionsDelay() > notificationByLanguage.getSuggestionsLastSentDate().getTime()) {
                sendSuggestionsInThread(user, suggestionStack);
                notificationByLanguage.setSuggestionsLastSentDate(now);
                suggestionStack.clear();
                notificationByLanguageService.save(notificationByLanguage);
            }
        }
        List<Song> newSongStack = notificationByLanguage.getNewSongStack();
        int newSongStackSize = newSongStack.size();
        if (newSongStackSize > 0 && notificationByLanguage.isNewSongs()) {
            Date now = new Date();
            if (newSongStackSize > 49 || now.getTime() - notificationByLanguage.getNewSongsDelay() > notificationByLanguage.getNewSongsLastSentDate().getTime()) {
                sendNewSongsInThread(user, newSongStack);
                notificationByLanguage.setNewSongsLastSentDate(now);
                newSongStack.clear();
                notificationByLanguageService.save(notificationByLanguage);
            }
        }
    }

    private void sendSuggestionsInThread(User user, List<Suggestion> suggestionStack) {
        List<Suggestion> suggestionList = new ArrayList<>(suggestionStack.size());
        suggestionList.addAll(suggestionStack);
        Thread thread = new Thread(() -> sendSuggestions(suggestionList, user));
        thread.start();
    }

    private void sendNewSongsInThread(User user, List<Song> songs) {
        List<Song> songList = new ArrayList<>(songs.size());
        songList.addAll(songs);
        Thread thread = new Thread(() -> sendNewSongs(songList, user));
        thread.start();
    }

    private void sendSuggestions(List<Suggestion> suggestions, User user) {
        try {
            final String freemarkerName = FreemarkerConfiguration.NEW_SUGGESTION + ".ftl";
            freemarker.template.Configuration config = ConfigurationUtil.getConfiguration();
            config.setDefaultEncoding("UTF-8");
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper;
            helper = new MimeMessageHelper(message, true);
            helper.setTo(new InternetAddress(user.getEmail()));
            helper.setFrom(new InternetAddress(getHostEmail()));
            if (suggestions.size() > 1) {
                helper.setSubject("New suggestions");
            } else {
                helper.setSubject("New suggestion");
            }

            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            Map<String, Object> model = createPattern(suggestions);
            template.process(model, writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
            sender.send(message);
        } catch (MessagingException | IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    private void sendNewSongs(List<Song> songs, User user) {
        try {
            final String freemarkerName = FreemarkerConfiguration.NEW_SONG + ".ftl";
            freemarker.template.Configuration config = ConfigurationUtil.getConfiguration();
            config.setDefaultEncoding("UTF-8");
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper;
            helper = new MimeMessageHelper(message, true);
            helper.setTo(new InternetAddress(user.getEmail()));
            helper.setFrom(new InternetAddress(getHostEmail()));
            if (songs.size() > 1) {
                helper.setSubject("New songs");
            } else {
                helper.setSubject("New song");
            }

            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            Map<String, Object> model = createPatternForSongs(songs);
            template.process(model, writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
            sender.send(message);
        } catch (MessagingException | IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> createPatternForSongs(List<Song> songs) {
        Map<String, Object> data = new HashMap<>();
        data.put("songs", songs);
        data.put("baseUrl", AppProperties.getInstance().baseUrl());
        return data;
    }

    private Map<String, Object> createPattern(List<Suggestion> suggestions) {
        Map<String, Object> data = new HashMap<>();
        List<SuggestionRow> suggestionRows = new ArrayList<>(suggestions.size());
        for (Suggestion suggestion : suggestions) {
            SuggestionRow suggestionRow = new SuggestionRow();
            suggestionRow.setSong(songService.findOneByUuid(suggestion.getSongUuid()));
            suggestionRow.setSuggestion(suggestion);
            String title = suggestion.getTitle();
            String suggestionType;
            if (title != null && !title.trim().isEmpty()) {
                suggestionType = "Title change";
            } else {
                List<SongVerse> verses = suggestion.getVerses();
                if (verses == null || verses.size() < 1 || verses.get(0).getText().trim().isEmpty()) {
                    suggestionType = "Only description";
                } else {
                    suggestionType = "";
                }
            }
            suggestionRow.setSuggestionType(suggestionType);
            suggestionRows.add(suggestionRow);
        }
        data.put("suggestionRows", suggestionRows);
        data.put("baseUrl", AppProperties.getInstance().baseUrl());
        return data;
    }

    private String getHostEmail() {
        return "noreply@songbook";
    }

    public void sendForgottenEmail(String email, String link) {
        try {
            final String freemarkerName = FreemarkerConfiguration.TOKEN_LINK_PAGE + ".html";
            freemarker.template.Configuration config = ConfigurationUtil.getConfiguration();
            config.setDefaultEncoding("UTF-8");
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper;
            helper = new MimeMessageHelper(message, true);
            helper.setTo(new InternetAddress(email));
            helper.setFrom(new InternetAddress(getHostEmail()));
            helper.setSubject("Forgotten password");

            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            Map<String, Object> model = createPatternForForgottenPassword(link);
            template.process(model, writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
            sender.send(message);
        } catch (MessagingException | IOException | TemplateException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> createPatternForForgottenPassword(String link) {
        Map<String, Object> data = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        final Date currentDate = new Date();
        data.put("currentDate", dateFormat.format(currentDate));
        data.put("tokenExpiryDate", dateFormat.format(currentDate.getTime() + TOKEN_DURATION));
        data.put("link", link);

        return data;
    }
}
