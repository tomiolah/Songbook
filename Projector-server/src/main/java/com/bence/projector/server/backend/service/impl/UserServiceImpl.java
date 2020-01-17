package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.UserRepository;
import com.bence.projector.server.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {
    @Autowired
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User registerUser(final User user) {
        if (user != null) {
            if (user.getRole() == null) {
                user.setRole(Role.ROLE_USER);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return this.save(user);
        }
        return null;
    }

    private boolean containsInList(Language language, List<Language> languages) {
        if (language == null || languages == null) {
            return false;
        }
        for (Language aLanguage : languages) {
            if (aLanguage.getId().equals(language.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> findAllReviewersByLanguage(Language language) {
        List<User> users = findAll();
        List<User> reviewers = new ArrayList<>();
        for (User user : users) {
            if (user.getRole().equals(Role.ROLE_REVIEWER) && containsInList(language, user.getReviewLanguages())) {
                reviewers.add(user);
            }
        }
        User byEmail = findByEmail("admin@email.com");
        boolean was = false;
        for (User user : reviewers) {
            if (user.getEmail().equals(byEmail.getEmail())) {
                was = true;
                break;
            }
        }
        if (!was) {
            reviewers.add(byEmail);
        }
        return reviewers;
    }
}
