package com.example.cursos.service;

import com.example.cursos.dto.ModuleRequest;
import com.example.cursos.dto.ModuleResponse;
import com.example.cursos.entity.CourseEntity;
import com.example.cursos.entity.ModuleEntity;
import com.example.cursos.exception.BusinessRuleException;
import com.example.cursos.exception.ResourceNotFoundException;
import com.example.cursos.mapper.CourseMapper;
import com.example.cursos.repository.ModuleRepository;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ModuleService {

    private final CourseService courseService;
    private final ModuleRepository moduleRepository;

    public ModuleService(CourseService courseService, ModuleRepository moduleRepository) {
        this.courseService = courseService;
        this.moduleRepository = moduleRepository;
    }

    @Transactional
    public ModuleResponse create(UUID courseId, ModuleRequest request) {
        CourseEntity course = courseService.loadCourse(courseId);
        int orderIndex = request.orderIndex() != null ? request.orderIndex() : nextOrder(course);
        validateOrderIndex(course, orderIndex, null);
        ModuleEntity module = ModuleEntity.builder()
                .course(course)
                .title(request.title())
                .summary(request.summary())
                .orderIndex(orderIndex)
                .build();
        course.getModules().add(module);
        ModuleEntity saved = moduleRepository.save(module);
        return CourseMapper.toModuleResponse(saved);
    }

    @Transactional
    public ModuleResponse update(UUID courseId, UUID moduleId, ModuleRequest request) {
        ModuleEntity module = loadModule(courseId, moduleId);
        int orderIndex = request.orderIndex() != null ? request.orderIndex() : module.getOrderIndex();
        validateOrderIndex(module.getCourse(), orderIndex, moduleId);
        module.setTitle(request.title());
        module.setSummary(request.summary());
        module.setOrderIndex(orderIndex);
        return CourseMapper.toModuleResponse(module);
    }

    @Transactional
    public void delete(UUID courseId, UUID moduleId) {
        ModuleEntity module = loadModule(courseId, moduleId);
        moduleRepository.delete(module);
    }

    ModuleEntity loadModule(UUID courseId, UUID moduleId) {
        return moduleRepository.findByCourse_IdAndId(courseId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Modulo no encontrado"));
    }

    private void validateOrderIndex(CourseEntity course, int orderIndex, UUID currentId) {
        course.getModules().stream()
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .filter(existing -> existing.getOrderIndex() == orderIndex)
                .findAny()
                .ifPresent(existing -> {
                    throw new BusinessRuleException("El orden del modulo ya existe en el curso");
                });
    }

    private int nextOrder(CourseEntity course) {
        return course.getModules().stream()
                .map(ModuleEntity::getOrderIndex)
                .max(Integer::compareTo)
                .orElse(-1) + 1;
    }
}
