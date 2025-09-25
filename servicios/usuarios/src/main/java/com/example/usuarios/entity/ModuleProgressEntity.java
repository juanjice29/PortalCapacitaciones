package com.example.usuarios.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "module_progress")
public class ModuleProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id")
    private CourseEnrollmentEntity enrollment;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EnrollmentStatus status;

    @Column(name = "completed_chapters", nullable = false)
    private int completedChapters;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @PrePersist
    void onCreate() {
        lastUpdated = Instant.now();
        if (status == null) {
            status = EnrollmentStatus.INICIADO;
        }
    }

    @PreUpdate
    void onUpdate() {
        lastUpdated = Instant.now();
    }
}
