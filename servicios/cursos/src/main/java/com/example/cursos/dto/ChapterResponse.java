package com.example.cursos.dto;

import java.time.Instant;
import java.util.UUID;

public record ChapterResponse(
        UUID id,
        String title,
        String content,
        int orderIndex,
        Integer durationMinutes,
        Instant createdAt,
        Instant updatedAt
) {
}
