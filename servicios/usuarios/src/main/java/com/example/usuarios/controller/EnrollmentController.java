package com.example.usuarios.controller;

import com.example.usuarios.dto.CreateEnrollmentRequest;
import com.example.usuarios.dto.EnrollmentResponse;
import com.example.usuarios.dto.ModuleProgressRequest;
import com.example.usuarios.dto.ModuleProgressResponse;
import com.example.usuarios.dto.UpdateEnrollmentStatusRequest;
import com.example.usuarios.service.EnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios/{userId}/inscripciones")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','INSTRUCTOR')")
    public EnrollmentResponse enroll(@PathVariable UUID userId, @Valid @RequestBody CreateEnrollmentRequest request) {
        return enrollmentService.enroll(userId, request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','INSTRUCTOR')")
    public List<EnrollmentResponse> list(@PathVariable UUID userId) {
        return enrollmentService.listEnrollments(userId);
    }

    @PutMapping("/{enrollmentId}/estado")
    @PreAuthorize("hasAnyRole('USER','ADMIN','INSTRUCTOR')")
    public EnrollmentResponse updateStatus(@PathVariable UUID userId,
                                           @PathVariable UUID enrollmentId,
                                           @Valid @RequestBody UpdateEnrollmentStatusRequest request) {
        return enrollmentService.updateStatus(userId, enrollmentId, request);
    }

    @PostMapping("/{enrollmentId}/modulos")
    @PreAuthorize("hasAnyRole('USER','ADMIN','INSTRUCTOR')")
    public ModuleProgressResponse upsertModule(@PathVariable UUID userId,
                                               @PathVariable UUID enrollmentId,
                                               @Valid @RequestBody ModuleProgressRequest request) {
        return enrollmentService.upsertModuleProgress(userId, enrollmentId, request);
    }
}
