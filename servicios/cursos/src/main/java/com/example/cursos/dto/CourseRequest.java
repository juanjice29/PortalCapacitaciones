package com.example.cursos.dto;

import com.example.cursos.entity.CourseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequest(
        @NotBlank @Size(max = 60) String code,
        @NotBlank @Size(max = 255) String title,
        String description,
        @NotNull CourseStatus status
) {
}
