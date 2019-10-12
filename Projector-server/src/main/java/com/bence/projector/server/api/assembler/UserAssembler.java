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
        userDTO.setRole(user.getRole().getValue());
        userDTO.setSurname(user.getSurname());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setActivated(user.isActivated());
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
            user.setRole(Role.getInstance(userDTO.getRole()));
            user.setSurname(userDTO.getSurname());
            user.setFirstName(userDTO.getFirstName());
        }
        return user;
    }
}
