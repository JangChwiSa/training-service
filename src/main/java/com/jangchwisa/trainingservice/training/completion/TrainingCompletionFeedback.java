package com.jangchwisa.trainingservice.training.completion;

public record TrainingCompletionFeedback(
        String feedbackType,
        String feedbackSource,
        String summary,
        String detailText
) {
}
