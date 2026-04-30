package com.jangchwisa.trainingservice.training.completion;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;
import com.jangchwisa.trainingservice.event.outbox.OutboxEventRepository;
import com.jangchwisa.trainingservice.training.feedback.entity.TrainingFeedback;
import com.jangchwisa.trainingservice.training.feedback.repository.TrainingFeedbackRepository;
import com.jangchwisa.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.score.entity.TrainingScore;
import com.jangchwisa.trainingservice.training.score.repository.TrainingScoreRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.summary.entity.TrainingSessionSummary;
import com.jangchwisa.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingCompletionService {

    private static final String EVENT_TYPE_TRAINING_COMPLETED = "TrainingCompleted";
    private static final String AGGREGATE_TYPE_TRAINING_SESSION = "TrainingSession";
    private static final String OUTBOX_STATUS_PENDING = "PENDING";
    private static final int DEFAULT_MAX_RETRY_COUNT = 5;

    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingScoreRepository trainingScoreRepository;
    private final TrainingFeedbackRepository trainingFeedbackRepository;
    private final TrainingProgressRepository trainingProgressRepository;
    private final TrainingSessionSummaryRepository trainingSessionSummaryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final Clock clock;

    public TrainingCompletionService(
            TrainingSessionRepository trainingSessionRepository,
            TrainingScoreRepository trainingScoreRepository,
            TrainingFeedbackRepository trainingFeedbackRepository,
            TrainingProgressRepository trainingProgressRepository,
            TrainingSessionSummaryRepository trainingSessionSummaryRepository,
            OutboxEventRepository outboxEventRepository,
            Clock clock
    ) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingScoreRepository = trainingScoreRepository;
        this.trainingFeedbackRepository = trainingFeedbackRepository;
        this.trainingProgressRepository = trainingProgressRepository;
        this.trainingSessionSummaryRepository = trainingSessionSummaryRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.clock = clock;
    }

    @Transactional
    public TrainingCompletionResult complete(TrainingCompletionCommand command) {
        TrainingSession session = trainingSessionRepository.findById(command.sessionId())
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Training session was not found."));
        validateSession(command, session);

        LocalDateTime completedAt = LocalDateTime.now(clock);
        validateCompletionData(command);

        command.originalDataWriter().run();
        trainingScoreRepository.save(toScore(command, completedAt));
        trainingFeedbackRepository.save(toFeedback(command, completedAt));
        trainingProgressRepository.applyCompletion(toProgress(command, completedAt));
        trainingSessionSummaryRepository.save(toSummary(command, completedAt));

        TrainingSession completedSession = session.complete(completedAt);
        trainingSessionRepository.update(completedSession);
        outboxEventRepository.save(toOutboxEvent(command, completedAt));

        return new TrainingCompletionResult(
                command.sessionId(),
                command.score().score(),
                command.feedback().summary(),
                true,
                completedAt
        );
    }

    private void validateSession(TrainingCompletionCommand command, TrainingSession session) {
        if (session.userId() != command.userId()) {
            throw new TrainingServiceException(ErrorCode.FORBIDDEN, "Training session does not belong to current user.");
        }
        if (session.trainingType() != command.trainingType()) {
            throw new TrainingServiceException(ErrorCode.CONFLICT, "Training session type does not match completion type.");
        }
        if (session.status() != TrainingSessionStatus.IN_PROGRESS) {
            throw new TrainingServiceException(ErrorCode.CONFLICT, "Training session is already completed or closed.");
        }
    }

    private void validateCompletionData(TrainingCompletionCommand command) {
        TrainingType trainingType = command.trainingType();
        if ((trainingType == TrainingType.SAFETY || trainingType == TrainingType.DOCUMENT)
                && (command.score().correctCount() == null || command.score().totalCount() == null)) {
            throw new TrainingServiceException(
                    ErrorCode.VALIDATION_ERROR,
                    "Choice or answer based completion requires correct and total counts."
            );
        }
        if (trainingType == TrainingType.FOCUS
                && (command.progress().currentLevel() == null
                || command.progress().highestUnlockedLevel() == null
                || command.progress().playedLevel() == null)) {
            throw new TrainingServiceException(
                    ErrorCode.VALIDATION_ERROR,
                    "Focus completion requires current level, highest unlocked level, and played level."
            );
        }
    }

    private TrainingScore toScore(TrainingCompletionCommand command, LocalDateTime completedAt) {
        TrainingCompletionScore score = command.score();
        return new TrainingScore(
                command.sessionId(),
                score.score(),
                score.scoreType(),
                score.correctCount(),
                score.totalCount(),
                score.accuracyRate(),
                score.wrongCount(),
                score.averageReactionMs(),
                score.rawMetricsJson(),
                completedAt
        );
    }

    private TrainingFeedback toFeedback(TrainingCompletionCommand command, LocalDateTime completedAt) {
        TrainingCompletionFeedback feedback = command.feedback();
        return new TrainingFeedback(
                command.sessionId(),
                feedback.feedbackType(),
                feedback.feedbackSource(),
                feedback.summary(),
                feedback.detailText(),
                completedAt
        );
    }

    private TrainingProgressCompletion toProgress(TrainingCompletionCommand command, LocalDateTime completedAt) {
        TrainingCompletionScore score = command.score();
        TrainingCompletionProgress progress = command.progress();
        return new TrainingProgressCompletion(
                command.userId(),
                command.sessionId(),
                command.trainingType(),
                score.score(),
                command.feedback().summary(),
                score.correctCount(),
                score.totalCount(),
                progress.currentLevel(),
                progress.highestUnlockedLevel(),
                progress.playedLevel(),
                score.accuracyRate() == null ? progress.accuracyRate() : score.accuracyRate(),
                score.averageReactionMs() == null ? progress.averageReactionMs() : score.averageReactionMs(),
                completedAt
        );
    }

    private TrainingSessionSummary toSummary(TrainingCompletionCommand command, LocalDateTime completedAt) {
        TrainingCompletionScore score = command.score();
        TrainingCompletionSummary summary = command.summary();
        return new TrainingSessionSummary(
                command.sessionId(),
                command.userId(),
                command.trainingType(),
                summary.scenarioId(),
                summary.scenarioTitle(),
                summary.category(),
                summary.title(),
                score.score(),
                summary.summaryText(),
                command.feedback().summary(),
                summary.correctCount() == null ? score.correctCount() : summary.correctCount(),
                summary.totalCount() == null ? score.totalCount() : summary.totalCount(),
                summary.accuracyRate() == null ? score.accuracyRate() : summary.accuracyRate(),
                summary.wrongCount() == null ? score.wrongCount() : summary.wrongCount(),
                summary.playedLevel(),
                summary.averageReactionMs() == null ? score.averageReactionMs() : summary.averageReactionMs(),
                completedAt,
                completedAt
        );
    }

    private OutboxEvent toOutboxEvent(TrainingCompletionCommand command, LocalDateTime completedAt) {
        String eventId = "evt-" + UUID.randomUUID();
        return new OutboxEvent(
                eventId,
                EVENT_TYPE_TRAINING_COMPLETED,
                AGGREGATE_TYPE_TRAINING_SESSION,
                command.sessionId(),
                command.sessionId(),
                command.userId(),
                command.trainingType(),
                toPayloadJson(eventId, command, completedAt),
                OUTBOX_STATUS_PENDING,
                0,
                DEFAULT_MAX_RETRY_COUNT,
                null,
                null,
                null,
                null,
                completedAt,
                completedAt
        );
    }

    private String toPayloadJson(String eventId, TrainingCompletionCommand command, LocalDateTime completedAt) {
        return """
                {"eventId":"%s","eventType":"%s","userId":%d,"sessionId":%d,"trainingType":"%s","score":%d,"scoreType":"%s","completedAt":"%s"}
                """.formatted(
                escapeJson(eventId),
                EVENT_TYPE_TRAINING_COMPLETED,
                command.userId(),
                command.sessionId(),
                command.trainingType().name(),
                command.score().score(),
                escapeJson(command.score().scoreType()),
                completedAt
        ).trim();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
