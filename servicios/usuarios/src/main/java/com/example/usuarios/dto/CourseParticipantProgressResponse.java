package com.example.usuarios.dto;

import com.example.usuarios.entity.EnrollmentStatus;
import java.time.Instant;
import java.util.UUID;

public record CourseParticipantProgressResponse(
        UUID enrollmentId,
        UUID userId,
        String userEmail,
        String fullName,
        EnrollmentStatus status,
        Instant enrolledAt,
        Instant lastStatusChange
) {
}
