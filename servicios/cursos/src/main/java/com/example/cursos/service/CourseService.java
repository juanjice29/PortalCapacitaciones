package com.example.cursos.service;

import com.example.cursos.dto.CourseRequest;
import com.example.cursos.dto.CourseResponse;
import com.example.cursos.dto.CourseSummaryResponse;
import com.example.cursos.entity.CourseEntity;
import com.example.cursos.exception.BusinessRuleException;
import com.example.cursos.exception.ResourceNotFoundException;
import com.example.cursos.mapper.CourseMapper;
import com.example.cursos.repository.CourseRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseSummaryResponse> findAll() {
        return courseRepository.findAll().stream()
                .map(CourseMapper::toSummary)
                .collect(Collectors.toList());
    }

    public CourseResponse findById(UUID id) {
        CourseEntity course = courseRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
        return CourseMapper.toResponse(course);
    }

    @Transactional
    public CourseSummaryResponse create(CourseRequest request, UUID actorId) {
        courseRepository.findByCode(request.code()).ifPresent(existing -> {
            throw new BusinessRuleException("El codigo de curso ya existe");
        });

        CourseEntity course = CourseEntity.builder()
                .code(request.code())
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .createdBy(actorId)
                .updatedBy(actorId)
                .build();
        CourseEntity saved = courseRepository.save(course);
        return CourseMapper.toSummary(saved);
    }

    @Transactional
    public CourseSummaryResponse update(UUID id, CourseRequest request, UUID actorId) {
        CourseEntity course = loadCourse(id);
        courseRepository.findByCode(request.code())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("El codigo de curso ya existe");
                });
        course.setCode(request.code());
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setStatus(request.status());
        course.setUpdatedBy(actorId);
        return CourseMapper.toSummary(course);
    }

    @Transactional
    public void delete(UUID id) {
        CourseEntity course = loadCourse(id);
        courseRepository.delete(course);
    }

    public CourseEntity loadCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
    }
}
