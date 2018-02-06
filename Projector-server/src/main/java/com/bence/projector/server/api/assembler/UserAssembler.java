package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.UserDTO;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserAssembler implements GeneralAssembler<User, UserDTO> {

    @Override
    public UserDTO createDto(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUuid(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setPreferredLanguage(user.getPreferredLanguage());
        userDTO.setRole(user.getRole() == null ? null : user.getRole().toString());
        return userDTO;
    }

    @Override
    public User createModel(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        return updateModel(user, userDTO);
    }

    @Override
    public User updateModel(User user, UserDTO userDTO) {
        if (user != null) {
            user.setEmail(userDTO.getEmail());
            user.setPreferredLanguage(userDTO.getPreferredLanguage());
            user.setRole(userDTO.getRole() == null ? null : Role.valueOf(userDTO.getRole()));
        }
        return user;
    }
}
