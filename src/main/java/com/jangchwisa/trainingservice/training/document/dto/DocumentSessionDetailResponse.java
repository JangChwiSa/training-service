package com.jangchwisa.trainingservice.training.document.dto;

import java.util.List;

public record DocumentSessionDetailResponse(
        long sessionId,
        int score,
        DocumentAnswerSummaryResponse answerSummary,
        List<DocumentAnswerDetailResponse> answers
) {
}
