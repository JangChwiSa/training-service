package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SafetySessionDetailResponse(
        @Schema(description = "조회한 안전 훈련 세션 ID입니다.", example = "20")
        long sessionId,
        @Schema(description = "안전 훈련 중 사용자가 남긴 선택 이력입니다.")
        List<SafetyActionLogResponse> actionLogs
) {
}
