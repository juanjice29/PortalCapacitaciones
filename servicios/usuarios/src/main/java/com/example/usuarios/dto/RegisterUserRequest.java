package com.example.usuarios.dto;

import com.example.usuarios.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        @Email @NotBlank String email,
        String password,
        @NotBlank String fullName,
        @NotNull Role role
) {
}
