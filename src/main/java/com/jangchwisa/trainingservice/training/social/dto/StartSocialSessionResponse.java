package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record StartSocialSessionResponse(
        @Schema(description = "생성된 사회성 훈련 세션 ID입니다. 이후 상세 조회 또는 완료 요청의 path 값으로 사용합니다.", example = "10")
        long sessionId,
        @Schema(description = "세션에 연결된 사회성 시나리오 ID입니다.", example = "1")
        long scenarioId,
        @Schema(description = "세션 진행 상태입니다.", example = "IN_PROGRESS")
        TrainingSessionStatus status
) {
}
