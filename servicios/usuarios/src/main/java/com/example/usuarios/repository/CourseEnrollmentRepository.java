package com.example.usuarios.repository;

import com.example.usuarios.entity.CourseEnrollmentEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollmentEntity, UUID> {

    @EntityGraph(attributePaths = {"modules", "user"})
    Optional<CourseEnrollmentEntity> findByUser_IdAndCourseId(UUID userId, UUID courseId);

    @EntityGraph(attributePaths = {"modules"})
    List<CourseEnrollmentEntity> findByUser_Id(UUID userId);

    @EntityGraph(attributePaths = {"modules", "user"})
    List<CourseEnrollmentEntity> findByCourseId(UUID courseId);
}
