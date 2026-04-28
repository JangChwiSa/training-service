package com.jangchwisa.trainingservice.training.summary.dto;

public record InternalTrainingSummaryResponse(
        Integer socialRecentScore,
        int safetyCorrectCount,
        int safetyTotalCount,
        int documentCorrectCount,
        int documentTotalCount,
        int focusCurrentLevel
) {
}
