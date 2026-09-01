package com.vanhuy.user_service.component;

import com.vanhuy.user_service.dto.CreateUserRequest;
import com.vanhuy.user_service.dto.UserDTO;
import com.vanhuy.user_service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .address(user.getAddress())
                .profileImageName(user.getProfileImageName())
                .roles(user.getRoles())
                .isActive(user.isActive())
                .build();
    }

    public User toEntity(CreateUserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setAddress(userRequest.getAddress());
        user.setProfileImageName(userRequest.getProfileImageName());
        user.setRoles(userRequest.getRoles());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        return user;
    }
}
