package com.example.cursos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChapterRequest(
        @NotBlank @Size(max = 255) String title,
        String content,
        @NotNull @Min(0) Integer orderIndex,
        @Min(0) Integer durationMinutes
) {
}
