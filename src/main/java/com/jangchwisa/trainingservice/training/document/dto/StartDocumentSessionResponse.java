package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StartDocumentSessionResponse(
        @Schema(description = "생성된 문서 이해 훈련 세션 ID입니다. 답변 제출 또는 상세 조회 요청의 path 값으로 사용합니다.", example = "50")
        long sessionId,
        @Schema(description = "이번 문서 이해 세션에서 풀어야 할 문제 목록입니다.")
        List<DocumentQuestionResponse> questions
) {
}
