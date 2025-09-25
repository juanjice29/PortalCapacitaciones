package com.example.usuarios.dto;

import com.example.usuarios.entity.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public record ModuleProgressResponse(
        UUID id,
        UUID moduleId,
        EnrollmentStatus status,
        int completedChapters,
        Instant lastUpdated
) {
}
