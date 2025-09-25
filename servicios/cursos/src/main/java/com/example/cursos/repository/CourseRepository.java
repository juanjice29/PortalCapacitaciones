package com.example.cursos.repository;

import com.example.cursos.entity.CourseEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    Optional<CourseEntity> findByCode(String code);

    @EntityGraph(attributePaths = {"modules", "modules.chapters"})
    Optional<CourseEntity> findWithDetailsById(UUID id);
}
