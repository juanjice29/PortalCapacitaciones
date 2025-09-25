package com.example.usuarios.dto;

import com.example.usuarios.entity.EnrollmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID courseId,
        EnrollmentStatus status,
        Instant enrolledAt,
        Instant lastStatusChange,
        List<ModuleProgressResponse> modules
) {
}
