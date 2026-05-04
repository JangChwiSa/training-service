package com.didgo.trainingservice.training.session.entity;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import java.time.LocalDateTime;
import java.util.Objects;

public record TrainingSession(
        Long sessionId,
        long userId,
        TrainingType trainingType,
        String subType,
        Long scenarioId,
        TrainingSessionStatus status,
        int currentStep,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {

    public TrainingSession {
        if (userId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "User id must be positive.");
        }
        Objects.requireNonNull(trainingType, "trainingType must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        if (currentStep < 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Current step must not be negative.");
        }
        if (trainingType == TrainingType.FOCUS && isBlank(subType)) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Focus training session requires sub type.");
        }
        if (endedAt != null && endedAt.isBefore(startedAt)) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Ended time must not be before started time.");
        }
    }

    public static TrainingSession start(
            long userId,
            TrainingType trainingType,
            String subType,
            Long scenarioId,
            LocalDateTime startedAt
    ) {
        return new TrainingSession(
                null,
                userId,
                trainingType,
                normalize(subType),
                scenarioId,
                TrainingSessionStatus.IN_PROGRESS,
                0,
                startedAt,
                null
        );
    }

    public TrainingSession withSessionId(long sessionId) {
        if (sessionId <= 0) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Session id must be positive.");
        }
        return new TrainingSession(
                sessionId,
                userId,
                trainingType,
                subType,
                scenarioId,
                status,
                currentStep,
                startedAt,
                endedAt
        );
    }

    public TrainingSession advanceToStep(int nextStep) {
        ensureInProgress("Only in-progress training session can update current step.");
        if (nextStep < currentStep) {
            throw new TrainingServiceException(ErrorCode.CONFLICT, "Current step cannot move backward.");
        }
        return new TrainingSession(
                sessionId,
                userId,
                trainingType,
                subType,
                scenarioId,
                status,
                nextStep,
                startedAt,
                endedAt
        );
    }

    public TrainingSession complete(LocalDateTime completedAt) {
        Objects.requireNonNull(completedAt, "completedAt must not be null");
        ensureInProgress("Only in-progress training session can be completed.");
        return new TrainingSession(
                sessionId,
                userId,
                trainingType,
                subType,
                scenarioId,
                TrainingSessionStatus.COMPLETED,
                currentStep,
                startedAt,
                completedAt
        );
    }

    public TrainingSession fail(LocalDateTime failedAt) {
        Objects.requireNonNull(failedAt, "failedAt must not be null");
        ensureInProgress("Only in-progress training session can be failed.");
        return new TrainingSession(
                sessionId,
                userId,
                trainingType,
                subType,
                scenarioId,
                TrainingSessionStatus.FAILED,
                currentStep,
                startedAt,
                failedAt
        );
    }

    private void ensureInProgress(String message) {
        if (status != TrainingSessionStatus.IN_PROGRESS) {
            throw new TrainingServiceException(ErrorCode.CONFLICT, message);
        }
    }

    private static String normalize(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
