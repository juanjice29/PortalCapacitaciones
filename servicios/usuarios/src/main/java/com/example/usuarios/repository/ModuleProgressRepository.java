package com.example.usuarios.repository;

import com.example.usuarios.entity.ModuleProgressEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgressEntity, UUID> {

    Optional<ModuleProgressEntity> findByEnrollment_IdAndModuleId(UUID enrollmentId, UUID moduleId);
}
