package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record NextSafetySceneRequest(
        @Schema(description = "현재 사용자가 선택지를 고른 장면 ID입니다. 세션 시작 또는 이전 next-scene 응답의 sceneId를 넣습니다.", example = "1")
        @Positive long sceneId,
        @Schema(description = "사용자가 선택한 선택지 ID입니다. 현재 장면의 choices 배열에서 받은 choiceId를 넣습니다.", example = "1")
        @Positive long choiceId
) {
}
