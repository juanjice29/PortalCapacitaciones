package com.example.cursos.controller;

import com.example.cursos.dto.CourseRequest;
import com.example.cursos.dto.CourseResponse;
import com.example.cursos.dto.CourseSummaryResponse;
import com.example.cursos.security.JwtUserPrincipal;
import com.example.cursos.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cursos")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<CourseSummaryResponse> list() {
        return courseService.findAll();
    }

    @GetMapping("/{courseId}")
    public CourseResponse detail(@PathVariable UUID courseId) {
        return courseService.findById(courseId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public CourseSummaryResponse create(@Valid @RequestBody CourseRequest request, Authentication authentication) {
        UUID actorId = extractUserId(authentication);
        return courseService.create(request, actorId);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public CourseSummaryResponse update(@PathVariable UUID courseId,
                                        @Valid @RequestBody CourseRequest request,
                                        Authentication authentication) {
        UUID actorId = extractUserId(authentication);
        return courseService.update(courseId, request, actorId);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public void delete(@PathVariable UUID courseId) {
        courseService.delete(courseId);
    }

    private UUID extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserPrincipal jwtPrincipal) {
            return jwtPrincipal.getId();
        }
        return null;
    }
}
