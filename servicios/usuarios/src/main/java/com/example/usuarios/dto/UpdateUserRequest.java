package com.example.usuarios.dto;

import com.example.usuarios.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank String fullName,
        @NotNull Role role,
        boolean enabled
) {
}
