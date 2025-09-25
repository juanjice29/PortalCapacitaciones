package com.example.usuarios.service;

import com.example.usuarios.dto.CourseParticipantProgressResponse;
import com.example.usuarios.dto.CourseProgressSummaryResponse;
import com.example.usuarios.dto.CreateEnrollmentRequest;
import com.example.usuarios.dto.EnrollmentResponse;
import com.example.usuarios.dto.ModuleProgressRequest;
import com.example.usuarios.dto.ModuleProgressResponse;
import com.example.usuarios.dto.UpdateEnrollmentStatusRequest;
import com.example.usuarios.dto.UserProgressSummaryResponse;
import com.example.usuarios.entity.CourseEnrollmentEntity;
import com.example.usuarios.entity.EnrollmentStatus;
import com.example.usuarios.entity.ModuleProgressEntity;
import com.example.usuarios.entity.UserEntity;
import com.example.usuarios.exception.BusinessRuleException;
import com.example.usuarios.exception.ResourceNotFoundException;
import com.example.usuarios.mapper.EnrollmentMapper;
import com.example.usuarios.repository.CourseEnrollmentRepository;
import com.example.usuarios.repository.ModuleProgressRepository;
import com.example.usuarios.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final ModuleProgressRepository moduleProgressRepository;
    private final UserRepository userRepository;

    public EnrollmentService(CourseEnrollmentRepository enrollmentRepository,
                             ModuleProgressRepository moduleProgressRepository,
                             UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.moduleProgressRepository = moduleProgressRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EnrollmentResponse enroll(UUID userId, CreateEnrollmentRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        enrollmentRepository.findByUser_IdAndCourseId(userId, request.courseId())
                .ifPresent(existing -> {
                    throw new BusinessRuleException("El usuario ya esta inscrito en el curso");
                });
        CourseEnrollmentEntity enrollment = CourseEnrollmentEntity.builder()
                .user(user)
                .courseId(request.courseId())
                .status(EnrollmentStatus.INICIADO)
                .build();
        CourseEnrollmentEntity saved = enrollmentRepository.save(enrollment);
        return EnrollmentMapper.toResponse(saved);
    }

    @Transactional
    public EnrollmentResponse updateStatus(UUID userId, UUID enrollmentId, UpdateEnrollmentStatusRequest request) {
        CourseEnrollmentEntity enrollment = loadEnrollment(enrollmentId);
        verifyOwnership(userId, enrollment);
        enrollment.setStatus(request.status());
        CourseEnrollmentEntity saved = enrollmentRepository.save(enrollment);
        return EnrollmentMapper.toResponse(saved);
    }

    @Transactional
    public ModuleProgressResponse upsertModuleProgress(UUID userId, UUID enrollmentId, ModuleProgressRequest request) {
        CourseEnrollmentEntity enrollment = loadEnrollment(enrollmentId);
        verifyOwnership(userId, enrollment);
        ModuleProgressEntity moduleProgress = moduleProgressRepository
                .findByEnrollment_IdAndModuleId(enrollment.getId(), request.moduleId())
                .map(entity -> updateModule(entity, request))
                .orElseGet(() -> createModule(enrollment, request));
        ModuleProgressEntity saved = moduleProgressRepository.save(moduleProgress);
        refreshEnrollmentStatus(enrollment);
        enrollmentRepository.save(enrollment);
        return EnrollmentMapper.toModuleResponse(saved);
    }

    public List<EnrollmentResponse> listEnrollments(UUID userId) {
        return enrollmentRepository.findByUser_Id(userId).stream()
                .map(EnrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserProgressSummaryResponse userProgress(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        List<EnrollmentResponse> enrollments = listEnrollments(userId);
        return new UserProgressSummaryResponse(user.getId(), user.getEmail(), user.getFullName(), enrollments);
    }

    public CourseProgressSummaryResponse courseProgress(UUID courseId) {
        List<CourseEnrollmentEntity> enrollments = enrollmentRepository.findByCourseId(courseId);
        if (enrollments.isEmpty()) {
            throw new ResourceNotFoundException("No hay inscripciones para el curso");
        }
        Map<String, Long> totals = enrollments.stream()
                .collect(Collectors.groupingBy(enrollment -> enrollment.getStatus().name(), Collectors.counting()));
        List<CourseParticipantProgressResponse> participants = enrollments.stream()
                .map(EnrollmentMapper::toParticipantResponse)
                .collect(Collectors.toList());
        return new CourseProgressSummaryResponse(courseId, totals, participants);
    }

    private void refreshEnrollmentStatus(CourseEnrollmentEntity enrollment) {
        boolean allCompleted = enrollment.getModules().stream()
                .allMatch(module -> module.getStatus() == EnrollmentStatus.COMPLETADO);
        if (allCompleted && !enrollment.getModules().isEmpty()) {
            enrollment.setStatus(EnrollmentStatus.COMPLETADO);
        } else {
            boolean anyInProgress = enrollment.getModules().stream()
                    .anyMatch(module -> module.getStatus() == EnrollmentStatus.EN_PROGRESO
                            || module.getStatus() == EnrollmentStatus.COMPLETADO);
            if (anyInProgress) {
                enrollment.setStatus(EnrollmentStatus.EN_PROGRESO);
            }
        }
    }

    private ModuleProgressEntity updateModule(ModuleProgressEntity entity, ModuleProgressRequest request) {
        entity.setStatus(request.status());
        entity.setCompletedChapters(request.completedChapters());
        return entity;
    }

    private ModuleProgressEntity createModule(CourseEnrollmentEntity enrollment, ModuleProgressRequest request) {
        ModuleProgressEntity entity = ModuleProgressEntity.builder()
                .enrollment(enrollment)
                .moduleId(request.moduleId())
                .status(request.status())
                .completedChapters(request.completedChapters())
                .build();
        enrollment.getModules().add(entity);
        return entity;
    }

    private CourseEnrollmentEntity loadEnrollment(UUID enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripcion no encontrada"));
    }

    private void verifyOwnership(UUID userId, CourseEnrollmentEntity enrollment) {
        if (!enrollment.getUser().getId().equals(userId)) {
            throw new BusinessRuleException("El usuario no tiene acceso a esta inscripcion");
        }
    }
}
