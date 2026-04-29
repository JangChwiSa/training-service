package com.jangchwisa.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record StartFocusSessionRequest(
        @Schema(description = "시작할 집중력 훈련 단계입니다. 현재 해금된 단계 이하의 양수를 입력합니다.", example = "1")
        @Positive int level
) {
}
