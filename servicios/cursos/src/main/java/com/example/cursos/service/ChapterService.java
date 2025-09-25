package com.example.cursos.service;

import com.example.cursos.dto.ChapterRequest;
import com.example.cursos.dto.ChapterResponse;
import com.example.cursos.entity.ChapterEntity;
import com.example.cursos.entity.ModuleEntity;
import com.example.cursos.exception.BusinessRuleException;
import com.example.cursos.exception.ResourceNotFoundException;
import com.example.cursos.mapper.CourseMapper;
import com.example.cursos.repository.ChapterRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChapterService {

    private final ModuleService moduleService;
    private final ChapterRepository chapterRepository;

    public ChapterService(ModuleService moduleService, ChapterRepository chapterRepository) {
        this.moduleService = moduleService;
        this.chapterRepository = chapterRepository;
    }

    @Transactional
    public ChapterResponse create(UUID courseId, UUID moduleId, ChapterRequest request) {
        ModuleEntity module = moduleService.loadModule(courseId, moduleId);
        validateOrderIndex(module, request.orderIndex(), null);
        ChapterEntity chapter = ChapterEntity.builder()
                .module(module)
                .title(request.title())
                .content(request.content())
                .orderIndex(request.orderIndex())
                .durationMinutes(request.durationMinutes())
                .build();
        module.getChapters().add(chapter);
        ChapterEntity saved = chapterRepository.save(chapter);
        return CourseMapper.toChapterResponse(saved);
    }

    @Transactional
    public ChapterResponse update(UUID courseId, UUID moduleId, UUID chapterId, ChapterRequest request) {
        ChapterEntity chapter = loadChapter(moduleId, chapterId);
        validateOrderIndex(chapter.getModule(), request.orderIndex(), chapterId);
        chapter.setTitle(request.title());
        chapter.setContent(request.content());
        chapter.setOrderIndex(request.orderIndex());
        chapter.setDurationMinutes(request.durationMinutes());
        return CourseMapper.toChapterResponse(chapter);
    }

    @Transactional
    public void delete(UUID courseId, UUID moduleId, UUID chapterId) {
        ChapterEntity chapter = loadChapter(moduleId, chapterId);
        chapterRepository.delete(chapter);
    }

    ChapterEntity loadChapter(UUID moduleId, UUID chapterId) {
        return chapterRepository.findByModule_IdAndId(moduleId, chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Capitulo no encontrado"));
    }

    private void validateOrderIndex(ModuleEntity module, int orderIndex, UUID currentId) {
        module.getChapters().stream()
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .filter(existing -> existing.getOrderIndex() == orderIndex)
                .findAny()
                .ifPresent(existing -> {
                    throw new BusinessRuleException("El orden del capitulo ya existe en el modulo");
                });
    }
}
