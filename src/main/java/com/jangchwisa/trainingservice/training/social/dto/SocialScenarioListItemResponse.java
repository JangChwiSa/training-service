package com.jangchwisa.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialScenarioListItemResponse(
        @Schema(description = "사회성 시나리오 ID입니다. 세션 시작 요청의 scenarioId로 사용합니다.", example = "1")
        long scenarioId,
        @Schema(description = "시나리오 제목입니다.", example = "Ask a coworker for help")
        String title,
        @Schema(description = "시나리오 난이도입니다.", example = "1")
        Integer difficulty
) {
}
