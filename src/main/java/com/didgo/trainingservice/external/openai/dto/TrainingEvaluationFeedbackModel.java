package com.didgo.trainingservice.external.openai.dto;

public record TrainingEvaluationFeedbackModel(
        String feedbackType,
        String feedbackSource,
        String summary,
        String detailText
) {
}
