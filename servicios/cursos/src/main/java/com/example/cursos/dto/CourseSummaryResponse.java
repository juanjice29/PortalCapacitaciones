package com.example.cursos.dto;

import com.example.cursos.entity.CourseStatus;
import java.time.Instant;
import java.util.UUID;

public record CourseSummaryResponse(
        UUID id,
        String code,
        String title,
        CourseStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
