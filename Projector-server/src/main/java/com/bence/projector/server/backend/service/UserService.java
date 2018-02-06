package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.User;

public interface UserService extends BaseService<User> {
    User findByEmail(String username);

    User registerUser(User user);
}
