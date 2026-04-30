package com.jangchwisa.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StartFocusSessionResponse(
        @Schema(description = "생성된 집중력 훈련 세션 ID입니다. 완료 요청의 path 값으로 사용합니다.", example = "40")
        long sessionId,
        @Schema(description = "이번 세션에서 플레이할 집중력 훈련 단계입니다.", example = "1")
        int level,
        @Schema(description = "세션 제한 시간입니다. 단위는 초입니다.", example = "180")
        int durationSeconds,
        @Schema(description = "이번 세션에서 순서대로 수행할 지시 목록입니다.")
        List<FocusCommandResponse> commands
) {
}
