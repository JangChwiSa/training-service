package com.jangchwisa.trainingservice.training.safety.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SafetySceneResponse(
        @Schema(description = "안전 훈련 장면 ID입니다. 다음 장면 진행 요청의 sceneId로 사용합니다.", example = "1")
        long sceneId,
        @Schema(description = "프론트가 장면을 표시할 때 참고할 화면 정보입니다.")
        String screenInfo,
        @Schema(description = "현재 안전 상황 설명입니다.")
        String situationText,
        @Schema(description = "사용자에게 제시할 질문입니다.")
        String questionText,
        @Schema(description = "현재 장면에서 선택 가능한 선택지 목록입니다.")
        List<SafetyChoiceResponse> choices,
        @Schema(description = "현재 장면이 마지막 장면인지 여부입니다.", example = "true")
        boolean endScene
) {
}
