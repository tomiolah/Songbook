package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.LoginSongDTO;
import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import com.bence.projector.server.api.assembler.SongAssembler;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
public class SongResource {

    private final SongRepository songRepository;
    private final SongService songService;
    private final SongAssembler songAssembler;
    private final SongTitleAssembler songTitleAssembler;
    private final StatisticsService statisticsService;
    private final UserService userService;
    private final JavaMailSender sender;
    private final LanguageService languageService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public SongResource(SongRepository songRepository, SongService songService, SongAssembler songAssembler, SongTitleAssembler songTitleAssembler, StatisticsService statisticsService, UserService userService, @Qualifier("javaMailSender") JavaMailSender sender, LanguageService languageService) {
        this.songRepository = songRepository;
        this.songService = songService;
        this.songAssembler = songAssembler;
        this.songTitleAssembler = songTitleAssembler;
        this.statisticsService = statisticsService;
        this.userService = userService;
        this.sender = sender;
        this.languageService = languageService;
    }

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

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitlesAfterModifiedDate/{lastModifiedDate}/language/{languageId}")
    public List<SongTitleDTO> getAllSongTitlesAfterModifiedDate(@PathVariable Long lastModifiedDate, HttpServletRequest httpServletRequest, @PathVariable("languageId") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        List<Song> songs = songService.findAllByLanguageAndModifiedDate(languageId, new Date(lastModifiedDate));
        return songTitleAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitlesInReview/language/{languageId}")
    public ResponseEntity<Object> getAllSongTitlesInReview(HttpServletRequest httpServletRequest, @PathVariable("languageId") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        Language language = languageService.findOne(languageId);
        final List<Song> all = songService.findAllInReviewByLanguage(language);
        return new ResponseEntity<>(songTitleAssembler.createDtoList(all), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songViews/language/{language}")
    public List<SongViewsDTO> getSongViewsByLanguage(HttpServletRequest httpServletRequest, @PathVariable("language") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        List<Song> songs = songService.findAllByLanguageContainingViews(languageId);
        List<SongViewsDTO> songViewsDTOS = new ArrayList<>(songs.size());
        for (Song song : songs) {
            SongViewsDTO dto = new SongViewsDTO();
            dto.setUuid(song.getId());
            dto.setViews(song.getViews());
            songViewsDTOS.add(dto);
        }
        return songViewsDTOS;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songFavourites/language/{language}")
    public List<SongFavouritesDTO> getSongFavouritesByLanguage(HttpServletRequest httpServletRequest, @PathVariable("language") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        List<Song> songs = songService.findAllByLanguageContainingFavourites(languageId);
        List<SongFavouritesDTO> songFavouritesDTOS = new ArrayList<>(songs.size());
        for (Song song : songs) {
            SongFavouritesDTO dto = new SongFavouritesDTO();
            dto.setUuid(song.getId());
            dto.setFavourites(song.getViews());
            songFavouritesDTOS.add(dto);
        }
        return songFavouritesDTOS;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/song")
    public SongDTO getSongByTitle(@RequestParam String title, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAllSongsLazy();
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

    @RequestMapping(method = RequestMethod.DELETE, value = "/reviewer/api/song/delete/{songId}")
    public ResponseEntity<Object> deleteSongByReviewer(Principal principal, @PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                final Song song = songService.findOne(songId);
                if (!songInReviewLanguages(user, song)) {
                    return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
                }
                song.setDeleted(true);
                song.setModifiedDate(new Date());
                song.setLastModifiedBy(user);
                songService.save(song);
                return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/song/erase/{songId}")
    public SongDTO eraseSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songService.findOne(songId);
        if (song.isDeleted()) {
            Language language = song.getLanguage();
            if (language != null) {
                songService.removeSongFromLanguage(song, language);
            }
            songService.delete(songId);
        }
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reviewer/api/song/erase/{songId}")
    public ResponseEntity<Object> eraseSongByReviewer(Principal principal, @PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                Song song = songService.findOne(songId);
                if (song != null && songInReviewLanguages(user, song)) {
                    song.setReviewerErased(true);
                    song.setModifiedDate(new Date());
                    song.setLastModifiedBy(user);
                    final Song savedSong = songService.save(song);
                    if (savedSong != null) {
                        return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
                    }
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
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

    @RequestMapping(method = RequestMethod.POST, value = "/user/api/song")
    public ResponseEntity<Object> createSong(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest, Principal principal) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = userService.findByEmail(principal.getName());
        if (user == null || user.isBanned()) {
            return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        final Song song = songAssembler.createModel(songDTO);
        song.setCreatedByEmail(user.getEmail());
        if (!user.isActivated()) {
            song.setDeleted(true);
            song.setUploaded(true);
        }
        final Date date = new Date();
        song.setCreatedDate(date);
        song.setModifiedDate(date);
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            Thread thread = new Thread(() -> {
                try {
                    sendEmail(song);
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

    private void sendEmail(Song song) throws MessagingException, MailSendException {
        List<User> reviewers = userService.findAllReviewersByLanguage(song.getLanguage());
        User byEmail = userService.findByEmail("bakobence@yahoo.com");
        reviewers.add(byEmail);
        for (User user : reviewers) {
            sendEmailToUser(song, user);
        }
    }

    private void sendEmailToUser(Song song, User user) throws MessagingException {
        final String freemarkerName = FreemarkerConfiguration.NEW_SONG + ".ftl";
        freemarker.template.Configuration config = ConfigurationUtil.getConfiguration();
        config.setDefaultEncoding("UTF-8");
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(new InternetAddress(user.getEmail()));
        helper.setFrom(new InternetAddress("noreply@songbook"));
        helper.setSubject("New song");

        try {
            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            template.process(createPattern(song), writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            helper.getMimeMessage().setContent("<div>\n" +
                    "    <h3>New song: " + song.getTitle() + "</h3>\n" +
                    "    <a href=\"" + AppProperties.getInstance().baseUrl() + "/#/song/" + song.getId() + "\">Link</a>\n" +
                    "  <h3>Email </h3><h4>" + song.getCreatedByEmail() + "</h4>" +
                    "</div>", "text/html;charset=utf-8");
        }
        sender.send(message);
    }

    private Map<String, Object> createPattern(Song song) {
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", AppProperties.getInstance().baseUrl());
        data.put("title", song.getTitle());
        data.put("songUuid", song.getId());
        String createdByEmail = song.getCreatedByEmail();
        data.put("email", createdByEmail == null ? "" : createdByEmail);

        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/song/upload")
    public ResponseEntity<Object> uploadSong(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songAssembler.createModel(songDTO);
        song.setOriginalId(songDTO.getUuid());
        song.setDeleted(true);
        song.setUploaded(true);
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            Thread thread = new Thread(() -> {
                try {
                    List<Song> songs = songService.findAllSongsLazy();
                    boolean deleted = false;
                    for (Song song1 : songs) {
                        if (!savedSong.getId().equals(song.getId()) && songService.matches(savedSong, song1)) {
                            songService.delete(savedSong.getId());
                            deleted = true;
                            break;
                        }
                    }
                    if (!deleted) {
                        sendEmail(savedSong);
                    }
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
        return new ResponseEntity<>(songTitleAssembler.createDtoList(all), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/api/song/{songId}")
    public ResponseEntity<Object> updateSongByAdmin(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                return updateSong(songId, songDTO, user);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reviewer/api/song/{songId}")
    public ResponseEntity<Object> updateSongByReviewer(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                Song song = songService.findOne(songId);
                if (song != null && songInReviewLanguages(user, song)) {
                    Date modifiedDate = song.getModifiedDate();
                    if (modifiedDate != null && modifiedDate.compareTo(songDTO.getModifiedDate()) != 0) {
                        return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
                    }
                    songDTO.setModifiedDate(new Date());
                    Song backUpSong = new Song(song);
                    backUpSong.setIsBackUp(true);
                    songRepository.save(backUpSong);
                    song.setBackUp(backUpSong);
                    song.setLastModifiedBy(user);
                    songAssembler.updateModel(song, songDTO);
                    final Song savedSong = songService.save(song);
                    if (savedSong != null) {
                        return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
                    }
                }
                return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    private boolean songInReviewLanguages(User user, Song song) {
        String id = song.getLanguage().getId();
        for (Language language : user.getReviewLanguages()) {
            if (language.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/removeDuplicates")
    public void removeDuplicates(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        List<Song> songs = songRepository.findAll();
        for (Song uploaded : songService.findAllByUploadedTrueAndDeletedTrue()) {
            for (Song song : songs) {
                if (!uploaded.getId().equals(song.getId()) && songService.matches(uploaded, song)) {
                    if (songRepository.findOne(song.getId()) != null) {
                        songService.delete(uploaded.getId());
                        break;
                    }
                }
            }
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/password/api/song/{songId}")
    public ResponseEntity<Object> updateSongByPassword(@PathVariable final String songId,
                                                       @RequestBody final LoginSongDTO loginSongDTO,
                                                       HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final User user = userService.findByEmail(loginSongDTO.getUsername());
        if (user != null) {
            if (getPasswordEncoder().matches(loginSongDTO.getPassword(), user.getPassword())) {
                return updateSong(songId, loginSongDTO.getSongDTO(), user);
            }
        }
        return null;
    }

    private ResponseEntity<Object> updateSong(String songId, SongDTO songDTO, User user) {
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
        song.setLastModifiedBy(user);
        song.setReviewerErased(null);
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

    @RequestMapping(method = RequestMethod.POST, value = "/api/songs/similar/song")
    public ResponseEntity<Object> similarSongsByPost(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songAssembler.createModel(songDTO);
        final List<Song> similar = songService.findAllSimilar(song);
        return new ResponseEntity<>(songAssembler.createDtoList(similar), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/api/song/{songId}/incViews")
    public ResponseEntity<Object> incrementViews(@PathVariable("songId") String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songService.findOne(songId);
        if (song != null) {
            song.incrementViews();
            song.setLastIncrementViewDate(new Date());
            songRepository.save(song);
            return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/api/song/{songId}/incFavourites")
    public ResponseEntity<Object> incrementFavourites(@PathVariable("songId") String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songService.findOne(songId);
        if (song != null) {
            song.incrementFavourites();
            song.setLastIncrementFavouritesDate(new Date());
            songRepository.save(song);
            return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST, value = "admin/api/songVersionGroup/{songId1}/{songId2}")
    public ResponseEntity<Object> mergeSongVersionGroup(@PathVariable("songId1") String songId1, @PathVariable("songId2") String songId2, HttpServletRequest httpServletRequest) {
        if (songId1.equals(songId2)) {
            return new ResponseEntity<>("Same song", HttpStatus.CONFLICT);
        }
        Date date = new Date();
        Song song1 = songService.findOne(songId1);
        Song song2 = songService.findOne(songId2);
        if (song1 == null || song2 == null) {
            return new ResponseEntity<>("Null", HttpStatus.NO_CONTENT);
        }
        saveStatistics(httpServletRequest, statisticsService);
        String song1VersionGroup = song1.getVersionGroup();
        String song2VersionGroup = song2.getVersionGroup();
        if (song1VersionGroup == null) {
            song1VersionGroup = song1.getId();
        }
        if (song2VersionGroup == null) {
            song2VersionGroup = song2.getId();
        }
        if (!song1VersionGroup.equals(song2VersionGroup)) {
            List<Song> allByVersionGroup1 = songService.findAllByVersionGroup(song1VersionGroup);
            List<Song> allByVersionGroup2 = songService.findAllByVersionGroup(song2VersionGroup);
            int size1 = allByVersionGroup1.size();
            int size2 = allByVersionGroup2.size();
            if (size1 == size2) {
                double sum1 = 0;
                for (Song song : allByVersionGroup1) {
                    sum1 += song.getModifiedDate().getTime();
                }
                double sum2 = 0;
                for (Song song : allByVersionGroup2) {
                    sum2 += song.getModifiedDate().getTime();
                }
                if (sum1 < sum2) {
                    ++size1;
                } else {
                    ++size2;
                }
            }
            if (size1 < size2) {
                for (Song song : allByVersionGroup1) {
                    song.setVersionGroup(song2VersionGroup);
                    song.setModifiedDate(date);
                }
                songRepository.save(allByVersionGroup1);
            } else {
                for (Song song : allByVersionGroup2) {
                    song.setVersionGroup(song1VersionGroup);
                    song.setModifiedDate(date);
                }
                songRepository.save(allByVersionGroup2);
            }
        }
        return new ResponseEntity<>("Merged", HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/versionGroup/{id}")
    public List<SongDTO> getSongsByVersionGroup(@PathVariable("id") String id) {
        List<Song> allByVersionGroup = songService.findAllByVersionGroup(id);
        return songAssembler.createDtoList(allByVersionGroup);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songsYoutube")
    public List<SongTitleDTO> getSongsContainingYoutubeUrl() {
        List<Song> allContainingYoutubeUrl = songService.findAllContainingYoutubeUrl();
        return songTitleAssembler.createDtoList(allContainingYoutubeUrl);
    }
}
