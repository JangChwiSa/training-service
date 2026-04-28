package com.jangchwisa.trainingservice.training.safety.dto;

import java.util.List;

public record SafetySessionDetailResponse(
        long sessionId,
        List<SafetyActionLogResponse> actionLogs
) {
}
