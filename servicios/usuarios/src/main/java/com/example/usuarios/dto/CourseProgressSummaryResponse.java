package com.example.usuarios.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CourseProgressSummaryResponse(
        UUID courseId,
        Map<String, Long> totalsByStatus,
        List<CourseParticipantProgressResponse> participants
) {
}
