package com.vanhuy.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @Pattern(regexp = "^$|.{6,}$", message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    private String profileImageName;
    private Set<String> roles;
    private boolean isActive;
}
