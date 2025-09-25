package com.example.usuarios.dto;

import com.example.usuarios.entity.EnrollmentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ModuleProgressRequest(
        @NotNull UUID moduleId,
        @NotNull EnrollmentStatus status,
        @Min(0) int completedChapters
) {
}
