package com.oj.api.dto;

import com.oj.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password,
            String fullName,
            LocalDate dob
    ) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record UserResponse(
            Long id,
            String username,
            String email,
            String fullName,
            LocalDate dob,
            Role role
    ) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
