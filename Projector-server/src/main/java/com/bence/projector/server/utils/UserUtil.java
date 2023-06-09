package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.UserService;

import java.util.ArrayList;
import java.util.List;

public class UserUtil {

    public static void markHadUploadedSongsForUsers(UserService userService, SongRepository songRepository) {
        List<User> users = userService.findAll();
        List<User> modifiedUsers = new ArrayList<>();
        for (User user : users) {
            List<Song> allByCreatedByEmail = songRepository.findAllByCreatedByEmail(user.getEmail());
            user.setHadUploadedSongs(allByCreatedByEmail != null && allByCreatedByEmail.size() > 0);
            if (user.isHadUploadedSongs()) {
                modifiedUsers.add(user);
            }
        }
        userService.saveAllByRepository(modifiedUsers);
    }
}
