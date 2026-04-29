package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialScenarioDetailResponse(
        @Schema(description = "사회성 시나리오 ID입니다.", example = "1")
        long scenarioId,
        @Schema(description = "시나리오가 속한 직무 유형입니다.", example = "OFFICE")
        SocialJobType jobType,
        @Schema(description = "시나리오 제목입니다.", example = "Ask a coworker for help")
        String title,
        @Schema(description = "훈련 상황의 배경 설명입니다.")
        String backgroundText,
        @Schema(description = "사용자가 대응해야 하는 구체적인 상황 설명입니다.")
        String situationText,
        @Schema(description = "대화 상대 또는 등장인물 정보입니다.")
        String characterInfo,
        @Schema(description = "시나리오 난이도입니다.", example = "1")
        Integer difficulty
) {
}
