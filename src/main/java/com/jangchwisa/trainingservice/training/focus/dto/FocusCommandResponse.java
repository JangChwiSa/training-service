package com.jangchwisa.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record FocusCommandResponse(
        @Schema(description = "집중력 훈련 지시 ID입니다. 완료 요청의 commandId로 사용합니다.", example = "1001")
        long commandId,
        @Schema(description = "세션 안에서 지시가 나타나는 순서입니다.", example = "1")
        int order,
        @Schema(description = "사용자에게 보여줄 지시 문구입니다.", example = "청기 들어")
        String commandText,
        @Schema(description = "정답으로 인정되는 기대 행동 값입니다.", example = "BLUE_UP")
        String expectedAction,
        @Schema(description = "세션 시작 후 지시가 표시될 시점입니다. 단위는 밀리초(ms)입니다.", example = "0")
        int displayAtMs
) {
}
