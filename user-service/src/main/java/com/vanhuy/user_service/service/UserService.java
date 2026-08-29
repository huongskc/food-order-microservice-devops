package com.vanhuy.user_service.service;

import com.vanhuy.user_service.component.UserMapper;
import com.vanhuy.user_service.dto.CreateUserRequest;
import com.vanhuy.user_service.dto.UpdateUserRequest;
import com.vanhuy.user_service.dto.UserDTO;
import com.vanhuy.user_service.exception.DuplicateUserException;
import com.vanhuy.user_service.exception.UserNotFoundException;
import com.vanhuy.user_service.model.User;
import com.vanhuy.user_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserDTO getByUsername(String username) {
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toUserDTO(user);
    }
    public Page<UserDTO> getAllActiveUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrue(pageable)
                .map(userMapper::toUserDTO);

    }
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserDTO);

    }

    @Transactional
    public void deleteUserById(Integer userId) {
        try {
            User user = userRepository.findByUserIdAndIsActiveTrue(userId);
            if (user == null) {
                throw new UserNotFoundException("User not found");
            }
            userRepository.deleteByUserId(userId);
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", userId);
            throw new UserNotFoundException(e.getMessage());
        }
    }

    // get user by id
    public UserDTO getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toUserDTO(user);
    }

    // new user
    public UserDTO create(CreateUserRequest userRequest) {
        validateNewUser(userRequest);

        User user = userMapper.toEntity(userRequest);
        user.setActive(true);
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of("ROLE_USER"));
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    public UserDTO updateUser(Integer id, UpdateUserRequest userRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (!existingUser.isActive()) {
            throw new UserNotFoundException("Cannot update inactive user");
        }

        validateUpdateUser(userRequest, existingUser);
        updateEntity(existingUser, userRequest);

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toUserDTO(updatedUser);
    }

    // update entity
    public void updateEntity(User user, UpdateUserRequest dto) {
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAddress(dto.getAddress());
        user.setProfileImageName(dto.getProfileImageName());
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            user.setRoles(dto.getRoles());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.isActive() != user.isActive()) {
            user.setActive(dto.isActive());
        }
    }

    public void deactivateUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public UserDTO reactivateUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setActive(true);
        User reactivatedUser = userRepository.save(user);
        return userMapper.toUserDTO(reactivatedUser);
    }

    private void validateNewUser(CreateUserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateUserException("Username is already taken");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateUserException("Email is already in use");
        }
    }

    private void validateUpdateUser(UpdateUserRequest userRequest, User existingUser) {
        if (!existingUser.getUsername().equals(userRequest.getUsername()) &&
                userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }
        if (!existingUser.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }
    }

}
