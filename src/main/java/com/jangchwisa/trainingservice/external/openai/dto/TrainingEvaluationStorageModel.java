package com.jangchwisa.trainingservice.external.openai.dto;

public record TrainingEvaluationStorageModel(
        TrainingEvaluationScoreModel score,
        TrainingEvaluationFeedbackModel feedback
) {
}
