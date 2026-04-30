package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StartSafetySessionResponse(
        @Schema(description = "생성된 안전 훈련 세션 ID입니다. 이후 다음 장면, 상세 조회, 완료 요청의 path 값으로 사용합니다.", example = "20")
        long sessionId,
        @Schema(description = "세션 시작 후 처음 진행할 안전 훈련 장면입니다.")
        SafetySceneResponse scene
) {
}
