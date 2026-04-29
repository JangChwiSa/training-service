package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentQuestionResponse(
        @Schema(description = "문서 이해 문제 ID입니다. 답변 제출 요청의 questionId로 사용합니다.", example = "1")
        long questionId,
        @Schema(description = "문서 문제 제목입니다.")
        String title,
        @Schema(description = "사용자가 읽고 이해해야 하는 문서 본문입니다.")
        String documentText,
        @Schema(description = "문서를 읽고 답해야 하는 질문입니다.")
        String questionText,
        @Schema(description = "문제 유형입니다.", example = "SHORT_ANSWER")
        String questionType
) {
}
