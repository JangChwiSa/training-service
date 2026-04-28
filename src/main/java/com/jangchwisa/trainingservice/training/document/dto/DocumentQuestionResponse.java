package com.jangchwisa.trainingservice.training.document.dto;

public record DocumentQuestionResponse(
        long questionId,
        String title,
        String documentText,
        String questionText,
        String questionType
) {
}
