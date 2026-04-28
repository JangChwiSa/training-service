package com.jangchwisa.trainingservice.training.document.dto;

public record DocumentAnswerResultResponse(
        long questionId,
        boolean correct,
        String correctAnswer,
        String explanation
) {
}
