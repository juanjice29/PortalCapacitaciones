package com.example.usuarios.mapper;

import com.example.usuarios.dto.CourseParticipantProgressResponse;
import com.example.usuarios.dto.EnrollmentResponse;
import com.example.usuarios.dto.ModuleProgressResponse;
import com.example.usuarios.entity.CourseEnrollmentEntity;
import com.example.usuarios.entity.ModuleProgressEntity;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class EnrollmentMapper {

    private EnrollmentMapper() {
    }

    public static EnrollmentResponse toResponse(CourseEnrollmentEntity entity) {
        List<ModuleProgressResponse> modules = entity.getModules().stream()
                .sorted(Comparator.comparing(ModuleProgressEntity::getLastUpdated).reversed())
                .map(EnrollmentMapper::toModuleResponse)
                .collect(Collectors.toList());

        return new EnrollmentResponse(
                entity.getId(),
                entity.getCourseId(),
                entity.getStatus(),
                entity.getEnrolledAt(),
                entity.getLastStatusChange(),
                modules
        );
    }

    public static ModuleProgressResponse toModuleResponse(ModuleProgressEntity entity) {
        return new ModuleProgressResponse(
                entity.getId(),
                entity.getModuleId(),
                entity.getStatus(),
                entity.getCompletedChapters(),
                entity.getLastUpdated()
        );
    }

    public static CourseParticipantProgressResponse toParticipantResponse(CourseEnrollmentEntity entity) {
        return new CourseParticipantProgressResponse(
                entity.getId(),
                entity.getUser().getId(),
                entity.getUser().getEmail(),
                entity.getUser().getFullName(),
                entity.getStatus(),
                entity.getEnrolledAt(),
                entity.getLastStatusChange()
        );
    }
}
