package com.example.usuarios.dto;

import com.example.usuarios.entity.AuthProvider;
import com.example.usuarios.entity.Role;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        AuthProvider provider,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
