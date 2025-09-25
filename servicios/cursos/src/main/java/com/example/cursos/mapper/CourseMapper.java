package com.example.cursos.mapper;

import com.example.cursos.dto.ChapterResponse;
import com.example.cursos.dto.CourseResponse;
import com.example.cursos.dto.CourseSummaryResponse;
import com.example.cursos.dto.ModuleResponse;
import com.example.cursos.entity.ChapterEntity;
import com.example.cursos.entity.CourseEntity;
import com.example.cursos.entity.ModuleEntity;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class CourseMapper {

    private CourseMapper() {
    }

    public static CourseSummaryResponse toSummary(CourseEntity entity) {
        return new CourseSummaryResponse(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static CourseResponse toResponse(CourseEntity entity) {
        List<ModuleResponse> modules = entity.getModules().stream()
                .sorted(Comparator.comparingInt(ModuleEntity::getOrderIndex))
                .map(CourseMapper::toModuleResponse)
                .collect(Collectors.toList());
        return new CourseResponse(
                entity.getId(),
                entity.getCode(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                modules
        );
    }

    public static ModuleResponse toModuleResponse(ModuleEntity entity) {
        List<ChapterResponse> chapters = entity.getChapters().stream()
                .sorted(Comparator.comparingInt(ChapterEntity::getOrderIndex))
                .map(CourseMapper::toChapterResponse)
                .collect(Collectors.toList());
        return new ModuleResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getOrderIndex(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                chapters
        );
    }

    public static ChapterResponse toChapterResponse(ChapterEntity entity) {
        return new ChapterResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getOrderIndex(),
                entity.getDurationMinutes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
