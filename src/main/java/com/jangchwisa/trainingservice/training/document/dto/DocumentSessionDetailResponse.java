package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record DocumentSessionDetailResponse(
        @Schema(description = "조회한 문서 이해 훈련 세션 ID입니다.", example = "50")
        long sessionId,
        @Schema(description = "문서 이해 훈련 점수입니다. 0부터 100 사이 값입니다.", example = "80")
        int score,
        @Schema(description = "문서 이해 훈련 정답 요약입니다.")
        DocumentAnswerSummaryResponse answerSummary,
        @Schema(description = "문제별 사용자 답변, 정답, 해설 상세 목록입니다.")
        List<DocumentAnswerDetailResponse> answers
) {
}
