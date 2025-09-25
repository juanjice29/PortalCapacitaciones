package com.example.cursos.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ModuleResponse(
        UUID id,
        String title,
        String summary,
        int orderIndex,
        Instant createdAt,
        Instant updatedAt,
        List<ChapterResponse> chapters
) {
}
