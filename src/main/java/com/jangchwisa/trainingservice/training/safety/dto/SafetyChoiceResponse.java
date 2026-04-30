package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SafetyChoiceResponse(
        @Schema(description = "선택지 ID입니다. 다음 장면 진행 요청의 choiceId로 사용합니다.", example = "1")
        long choiceId,
        @Schema(description = "사용자에게 보여줄 선택지 문구입니다.")
        String text
) {
}
