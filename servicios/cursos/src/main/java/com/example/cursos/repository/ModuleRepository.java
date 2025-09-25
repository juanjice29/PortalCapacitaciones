package com.example.cursos.repository;

import com.example.cursos.entity.ModuleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<ModuleEntity, UUID> {

    List<ModuleEntity> findByCourse_IdOrderByOrderIndex(UUID courseId);

    Optional<ModuleEntity> findByCourse_IdAndId(UUID courseId, UUID moduleId);
}
