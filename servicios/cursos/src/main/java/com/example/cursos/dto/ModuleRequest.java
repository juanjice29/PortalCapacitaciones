package com.example.cursos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModuleRequest(
        @NotBlank @Size(max = 255) String title,
        String summary,
        @Min(0) Integer orderIndex
) {
}
