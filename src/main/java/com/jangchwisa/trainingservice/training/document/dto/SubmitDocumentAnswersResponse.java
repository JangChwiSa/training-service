package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SubmitDocumentAnswersResponse(
        @Schema(description = "완료 처리된 문서 이해 훈련 세션 ID입니다.", example = "50")
        long sessionId,
        @Schema(description = "문서 이해 훈련 점수입니다. 0부터 100 사이 값입니다.", example = "100")
        int score,
        @Schema(description = "정답 수입니다.", example = "1")
        int correctCount,
        @Schema(description = "전체 문제 수입니다.", example = "1")
        int totalCount,
        @Schema(description = "문제별 채점 결과 목록입니다.")
        List<DocumentAnswerResultResponse> results,
        @Schema(description = "세션 완료 여부입니다.", example = "true")
        boolean completed
) {
}
