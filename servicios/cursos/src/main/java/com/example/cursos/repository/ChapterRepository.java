package com.example.cursos.repository;

import com.example.cursos.entity.ChapterEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<ChapterEntity, UUID> {

    List<ChapterEntity> findByModule_IdOrderByOrderIndex(UUID moduleId);

    Optional<ChapterEntity> findByModule_IdAndId(UUID moduleId, UUID chapterId);
}
