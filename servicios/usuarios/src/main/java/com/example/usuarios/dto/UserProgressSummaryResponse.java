package com.example.usuarios.dto;

import java.util.List;
import java.util.UUID;

public record UserProgressSummaryResponse(
        UUID userId,
        String userEmail,
        String fullName,
        List<EnrollmentResponse> enrollments
) {
}
