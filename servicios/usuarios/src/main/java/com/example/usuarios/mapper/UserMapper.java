package com.example.usuarios.mapper;

import com.example.usuarios.dto.UserResponse;
import com.example.usuarios.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getFullName(),
                entity.getRole(),
                entity.getProvider(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
