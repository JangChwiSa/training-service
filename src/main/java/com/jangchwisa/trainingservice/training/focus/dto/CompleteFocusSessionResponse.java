package com.jangchwisa.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record CompleteFocusSessionResponse(
        @Schema(description = "완료 처리된 집중력 훈련 세션 ID입니다.", example = "40")
        long sessionId,
        @Schema(description = "집중력 훈련 점수입니다. 0부터 100 사이 값입니다.", example = "92")
        int score,
        @Schema(description = "정확도입니다. 단위는 퍼센트입니다.", example = "92.5")
        BigDecimal accuracyRate,
        @Schema(description = "오답 반응 수입니다.", example = "3")
        int wrongCount,
        @Schema(description = "평균 반응 시간입니다. 단위는 밀리초(ms)입니다.", example = "820")
        int averageReactionMs,
        @Schema(description = "이번 결과로 다음 단계가 새로 해금됐는지 여부입니다.", example = "true")
        boolean unlockedNextLevel,
        @Schema(description = "완료 후 사용자 현재 집중력 단계입니다.", example = "3")
        int currentLevel,
        @Schema(description = "완료 후 사용자가 플레이할 수 있는 최고 해금 단계입니다.", example = "3")
        int highestUnlockedLevel
) {
}
