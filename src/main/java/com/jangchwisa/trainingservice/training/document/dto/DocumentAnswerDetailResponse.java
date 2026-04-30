package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DocumentAnswerDetailResponse(
        @Schema(description = "문서 문제 ID입니다.", example = "1")
        long questionId,
        @Schema(description = "문서 문제 질문입니다.")
        String questionText,
        @Schema(description = "사용자가 제출한 답변입니다.")
        String userAnswer,
        @Schema(description = "문제의 정답입니다.")
        String correctAnswer,
        @Schema(description = "사용자 답변이 정답인지 여부입니다.", example = "true")
        boolean correct,
        @Schema(description = "정답 판단에 대한 해설입니다.")
        String explanation
) {
}
