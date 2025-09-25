package com.example.cursos.controller;

import com.example.cursos.dto.CourseResponse;
import com.example.cursos.dto.ModuleRequest;
import com.example.cursos.dto.ModuleResponse;
import com.example.cursos.service.CourseService;
import com.example.cursos.service.ModuleService;
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
@RequestMapping("/cursos/{courseId}/modulos")
public class ModuleController {

    private final ModuleService moduleService;
    private final CourseService courseService;

    public ModuleController(ModuleService moduleService, CourseService courseService) {
        this.moduleService = moduleService;
        this.courseService = courseService;
    }

    @GetMapping
    public List<ModuleResponse> list(@PathVariable UUID courseId) {
        CourseResponse course = courseService.findById(courseId);
        return course.modules();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ModuleResponse create(@PathVariable UUID courseId, @Valid @RequestBody ModuleRequest request) {
        return moduleService.create(courseId, request);
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ModuleResponse update(@PathVariable UUID courseId,
                                 @PathVariable UUID moduleId,
                                 @Valid @RequestBody ModuleRequest request) {
        return moduleService.update(courseId, moduleId, request);
    }

    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public void delete(@PathVariable UUID courseId, @PathVariable UUID moduleId) {
        moduleService.delete(courseId, moduleId);
    }
}
