package com.jangchwisa.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FocusReactionRequest(
        @Schema(description = "반응한 지시 ID입니다. 집중력 세션 시작 응답의 commands 배열에서 받은 commandId를 넣습니다.", example = "1001")
        @Positive long commandId,
        @Schema(description = "사용자가 실제로 수행한 입력입니다. expectedAction과 비교해 정답 여부를 계산합니다.", example = "BLUE_UP")
        @NotBlank String userInput,
        @Schema(description = "지시가 표시된 뒤 사용자가 반응하기까지 걸린 시간입니다. 단위는 밀리초(ms)입니다.", example = "720")
        @PositiveOrZero int reactionMs
) {
}
