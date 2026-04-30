package com.jangchwisa.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialFeedbackResponse(
        @Schema(description = "훈련 결과에 대한 짧은 피드백 요약입니다.")
        String summary,
        @Schema(description = "훈련 결과에 대한 상세 피드백 문장입니다.")
        String detailText
) {
}
