package com.didgo.trainingservice.training.completion;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import java.util.Objects;

public record TrainingCompletionCommand(
        long userId,
        long sessionId,
        TrainingType trainingType,
        TrainingCompletionScore score,
        TrainingCompletionFeedback feedback,
        TrainingCompletionSummary summary,
        TrainingCompletionProgress progress,
        Runnable originalDataWriter
) {

    public TrainingCompletionCommand {
        if (userId <= 0 || sessionId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Completion ids must be positive.");
        }
        Objects.requireNonNull(trainingType, "trainingType must not be null");
        Objects.requireNonNull(score, "score must not be null");
        Objects.requireNonNull(feedback, "feedback must not be null");
        Objects.requireNonNull(summary, "summary must not be null");
        if (progress == null) {
            progress = TrainingCompletionProgress.none();
        }
        if (originalDataWriter == null) {
            originalDataWriter = () -> {
            };
        }
    }
}
