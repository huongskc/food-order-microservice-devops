package com.vanhuy.user_service.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vanhuy.user_service.dto.CreateUserRequest;
import com.vanhuy.user_service.dto.UpdateUserRequest;
import com.vanhuy.user_service.dto.UserDTO;
import com.vanhuy.user_service.model.User;
import com.vanhuy.user_service.repository.UserRepository;
import com.vanhuy.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserMapperTest {
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserMapper userMapper = new UserMapper(passwordEncoder);

    @Test
    void responseDtoNeverSerializesPassword() {
        User user = new User();
        user.setUserId(1);
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setRoles(Set.of("ROLE_USER"));

        UserDTO response = userMapper.toUserDTO(user);
        ObjectMapper objectMapper = new ObjectMapper();

        assertFalse(objectMapper.valueToTree(response).has("password"));
    }

    @Test
    void createRequestPasswordIsEncoded() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("user");
        request.setEmail("user@example.com");
        request.setPassword("secret1");
        when(passwordEncoder.encode("secret1")).thenReturn("encoded-secret");

        User user = userMapper.toEntity(request);

        assertEquals("encoded-secret", user.getPassword());
    }

    @Test
    void blankUpdatePasswordKeepsExistingPassword() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("encoded-existing");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("user");
        request.setEmail("user@example.com");
        request.setPassword("");

        UserService service = new UserService(
                mock(UserRepository.class), passwordEncoder, userMapper);
        service.updateEntity(user, request);

        assertEquals("encoded-existing", user.getPassword());
        verify(passwordEncoder, never()).encode("");
    }
}
