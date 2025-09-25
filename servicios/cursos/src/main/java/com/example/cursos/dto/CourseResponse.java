package com.example.cursos.dto;

import com.example.cursos.entity.CourseStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String code,
        String title,
        String description,
        CourseStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<ModuleResponse> modules
) {
}
