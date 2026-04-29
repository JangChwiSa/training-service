package com.jangchwisa.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CompleteFocusSessionRequest(
        @Schema(description = "집중력 훈련 중 기록한 전체 반응 로그입니다. 세션 시작 응답의 각 command에 대한 반응을 제출합니다.")
        @NotEmpty List<@Valid FocusReactionRequest> reactions
) {
}
