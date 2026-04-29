package com.jangchwisa.trainingservice.training.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DocumentAnswerRequest(
        @Schema(description = "답변할 문서 문제 ID입니다. 문서 이해 세션 시작 응답의 questions 배열에서 받은 questionId를 넣습니다.", example = "1")
        @Positive long questionId,
        @Schema(description = "사용자가 입력한 답변입니다. 빈 문자열은 사용할 수 없습니다.", example = "Room 2")
        @NotBlank String userAnswer
) {
}
