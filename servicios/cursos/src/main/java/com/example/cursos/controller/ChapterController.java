package com.example.cursos.controller;

import com.example.cursos.dto.ChapterRequest;
import com.example.cursos.dto.ChapterResponse;
import com.example.cursos.dto.CourseResponse;
import com.example.cursos.dto.ModuleResponse;
import com.example.cursos.exception.ResourceNotFoundException;
import com.example.cursos.service.ChapterService;
import com.example.cursos.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cursos/{courseId}/modulos/{moduleId}/capitulos")
public class ChapterController {

    private final ChapterService chapterService;
    private final CourseService courseService;

    public ChapterController(ChapterService chapterService, CourseService courseService) {
        this.chapterService = chapterService;
        this.courseService = courseService;
    }

    @GetMapping
    public List<ChapterResponse> list(@PathVariable UUID courseId, @PathVariable UUID moduleId) {
        CourseResponse course = courseService.findById(courseId);
        ModuleResponse module = course.modules().stream()
                .filter(m -> m.id().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Modulo no pertenece al curso"));
        return module.chapters();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ChapterResponse create(@PathVariable UUID courseId,
                                  @PathVariable UUID moduleId,
                                  @Valid @RequestBody ChapterRequest request) {
        return chapterService.create(courseId, moduleId, request);
    }

    @PutMapping("/{chapterId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ChapterResponse update(@PathVariable UUID courseId,
                                  @PathVariable UUID moduleId,
                                  @PathVariable UUID chapterId,
                                  @Valid @RequestBody ChapterRequest request) {
        return chapterService.update(courseId, moduleId, chapterId, request);
    }

    @DeleteMapping("/{chapterId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public void delete(@PathVariable UUID courseId,
                       @PathVariable UUID moduleId,
                       @PathVariable UUID chapterId) {
        chapterService.delete(courseId, moduleId, chapterId);
    }
}
