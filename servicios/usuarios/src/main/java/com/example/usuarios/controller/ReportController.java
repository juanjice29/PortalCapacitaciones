package com.example.usuarios.controller;

import com.example.usuarios.dto.CourseProgressSummaryResponse;
import com.example.usuarios.dto.UserProgressSummaryResponse;
import com.example.usuarios.service.EnrollmentService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportes")
public class ReportController {

    private final EnrollmentService enrollmentService;

    public ReportController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/usuarios/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public UserProgressSummaryResponse byUser(@PathVariable UUID userId) {
        return enrollmentService.userProgress(userId);
    }

    @GetMapping("/cursos/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public CourseProgressSummaryResponse byCourse(@PathVariable UUID courseId) {
        return enrollmentService.courseProgress(courseId);
    }
}
