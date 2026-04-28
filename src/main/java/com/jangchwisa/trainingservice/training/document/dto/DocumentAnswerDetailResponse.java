package com.jangchwisa.trainingservice.training.document.dto;

public record DocumentAnswerDetailResponse(
        long questionId,
        String questionText,
        String userAnswer,
        String correctAnswer,
        boolean correct,
        String explanation
) {
}
