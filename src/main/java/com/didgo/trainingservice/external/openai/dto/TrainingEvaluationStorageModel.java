package com.didgo.trainingservice.external.openai.dto;

public record TrainingEvaluationStorageModel(
        TrainingEvaluationScoreModel score,
        TrainingEvaluationFeedbackModel feedback
) {
}
