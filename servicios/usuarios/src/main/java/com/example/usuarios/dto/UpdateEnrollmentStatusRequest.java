package com.example.usuarios.dto;

import com.example.usuarios.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEnrollmentStatusRequest(@NotNull EnrollmentStatus status) {
}
