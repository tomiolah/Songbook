package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.LoginSongDTO;
import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.server.api.assembler.SongAssembler;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.FreemarkerConfiguration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
public class SongResource {

    @Autowired
    private SongService songService;
    @Autowired
    private SongAssembler songAssembler;
    @Autowired
    private SongTitleAssembler songTitleAssembler;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    @Qualifier("javaMailSender")
    @Autowired
    private JavaMailSender sender;
    @Autowired
    private FreemarkerConfiguration freemarkerConfiguration;

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs")
    public List<SongDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAll();
        return songAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/language/{languageId}")
    public List<SongDTO> findAll(@PathVariable("languageId") String languageId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAllByLanguage(languageId);
        return songAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/language/{languageId}/modifiedDate/{lastModifiedDate}")
    public List<SongDTO> findAll(@PathVariable("languageId") String languageId,
                                 @PathVariable("lastModifiedDate") Long lastModifiedDate,
                                 HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAllByLanguageAndModifiedDate(languageId, new Date(lastModifiedDate));
        return songAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songsAfterModifiedDate/{lastModifiedDate}")
    public List<SongDTO> getAllSongsAfterModifiedDate(@PathVariable Long lastModifiedDate, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAllAfterModifiedDate(new Date(lastModifiedDate));
        return songAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitles")
    public List<SongTitleDTO> getAllSongTitles(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAll();
        return songTitleAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitlesAfterModifiedDate/{lastModifiedDate}")
    public List<SongTitleDTO> getAllSongTitlesAfterModifiedDate(@PathVariable Long lastModifiedDate, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAllAfterModifiedDate(new Date(lastModifiedDate));
        return songTitleAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/song")
    public SongDTO getSongByTitle(@RequestParam String title, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAll();
        for (Song song : songs) {
            if (song.getTitle().equals(title)) {
                return songAssembler.createDto(song);
            }
        }
        return new SongDTO();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/song/{songId}")
    public SongDTO getSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songService.findOne(songId);
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/song/delete/{songId}")
    public SongDTO deleteSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songService.findOne(songId);
        song.setDeleted(true);
        song.setModifiedDate(new Date());
        songService.save(song);
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/song/erase/{songId}")
    public SongDTO eraseSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songService.findOne(songId);
        if (song.isDeleted()) {
            songService.delete(songId);
        }
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/song/publish/{songId}")
    public SongDTO publishSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songService.findOne(songId);
        song.setDeleted(false);
        song.setModifiedDate(new Date());
        songService.save(song);
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/song")
    public ResponseEntity<Object> createSong(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songAssembler.createModel(songDTO);
        final Date date = new Date();
        song.setCreatedDate(date);
        song.setModifiedDate(date);
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            SongDTO dto = songAssembler.createDto(savedSong);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendEmail(Song song)
            throws MessagingException, MailSendException {
        final String freemarkerName = FreemarkerConfiguration.NEW_SONG + ".ftl";
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
        helper.setSubject("Új ének");

        try {
            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            template.process(createPattern(song), writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            helper.getMimeMessage().setContent("<div>\n" +
                    "    <h3>Új ének: " + song.getTitle() + "</h3>\n" +
                    "    <a href=\"https://projector-songbook.herokuapp.com/#/song/" + song.getId() + "\">Link</a>\n" +
                    "</div>", "text/html;charset=utf-8");
        }
        sender.send(message);
    }

    private Map<String, Object> createPattern(Song song) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", song.getTitle());
        data.put("songUuid", song.getId());

        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/song/upload")
    public ResponseEntity<Object> uploadSong(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songAssembler.createModel(songDTO);
        song.setDeleted(true);
        song.setUploaded(true);
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            Thread thread = new Thread(() -> {
                try {
                    sendEmail(savedSong);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            SongDTO dto = songAssembler.createDto(savedSong);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/upload")
    public ResponseEntity<Object> uploadedSongs(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAllByUploadedTrueAndDeletedTrue();
        return new ResponseEntity<>(songAssembler.createDtoList(all), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/api/song/{songId}")
    public ResponseEntity<Object> updateSongByAdmin(@PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        return updateSong(songId, songDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/password/api/song/{songId}")
    public ResponseEntity<Object> updateSongByPassword(@PathVariable final String songId,
                                                       @RequestBody final LoginSongDTO loginSongDTO,
                                                       HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final User user = userService.findByEmail(loginSongDTO.getUsername());
        if (user != null) {
            if (getPasswordEncoder().matches(loginSongDTO.getPassword(), user.getPassword())) {
                return updateSong(songId, loginSongDTO.getSongDTO());
            }
        }
        return null;
    }

    private ResponseEntity<Object> updateSong(String songId, SongDTO songDTO) {
        Song song = songService.findOne(songId);
        if (song != null) {
            Date modifiedDate = song.getModifiedDate();
            if (modifiedDate != null && modifiedDate.compareTo(songDTO.getModifiedDate()) != 0) {
                return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
            }
            songDTO.setModifiedDate(new Date());
            songAssembler.updateModel(song, songDTO);
        } else {
            song = songAssembler.createModel(songDTO);
        }
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private PasswordEncoder getPasswordEncoder() {
        if (passwordEncoder == null) {
            passwordEncoder = new BCryptPasswordEncoder();
        }
        return passwordEncoder;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/similar/song/{songId}")
    public ResponseEntity<Object> similarSongs(@PathVariable("songId") String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songService.findOne(songId);
        final List<Song> similar = songService.findAllSimilar(song);
        return new ResponseEntity<>(songAssembler.createDtoList(similar), HttpStatus.ACCEPTED);
    }
}
