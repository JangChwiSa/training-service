package com.jangchwisa.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SocialSessionDetailResponse(
        @Schema(description = "조회한 사회성 훈련 세션 ID입니다.", example = "10")
        long sessionId,
        @Schema(description = "사회성 훈련 평가 점수입니다. 0부터 100 사이 값입니다.", example = "85")
        int score,
        @Schema(description = "점수 산정 방식입니다.", example = "AI_EVALUATION")
        String scoreType,
        @Schema(description = "AI가 생성한 사회성 훈련 피드백입니다.")
        SocialFeedbackResponse feedback,
        @Schema(description = "훈련 완료 시 저장된 전체 대화 로그입니다.")
        List<SocialDialogLogResponse> dialogLogs
) {
}
