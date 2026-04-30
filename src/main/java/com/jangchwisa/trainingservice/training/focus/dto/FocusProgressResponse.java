package com.jangchwisa.trainingservice.training.focus.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

public record FocusProgressResponse(
        @Schema(description = "사용자의 현재 집중력 훈련 단계입니다.", example = "3")
        int currentLevel,
        @Schema(description = "사용자가 플레이할 수 있는 최고 해금 단계입니다.", example = "3")
        int highestUnlockedLevel,
        @Schema(description = "가장 최근에 플레이한 단계입니다. 기록이 없으면 null입니다.", example = "2")
        Integer lastPlayedLevel,
        @Schema(description = "최근 플레이 정확도입니다. 단위는 퍼센트이며 기록이 없으면 null입니다.", example = "92.5")
        BigDecimal lastAccuracyRate,
        @Schema(description = "최근 플레이 평균 반응 시간입니다. 단위는 밀리초(ms)이며 기록이 없으면 null입니다.", example = "820")
        Integer lastAverageReactionMs
) {
}
