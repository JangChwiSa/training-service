package com.didgo.trainingservice.training.progress.entity;

public record MonthlyTrainingSummaryEntry(
        Integer score,
        String category,
        Integer playedLevel,
        String sessionSubType
) {
}
