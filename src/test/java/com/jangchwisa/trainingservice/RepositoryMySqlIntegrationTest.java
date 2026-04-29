package com.jangchwisa.trainingservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;
import com.jangchwisa.trainingservice.event.outbox.OutboxEventRepository;
import com.jangchwisa.trainingservice.training.progress.entity.MonthlyTrainingSummaryEntry;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.jangchwisa.trainingservice.training.summary.entity.TrainingSessionSummary;
import com.jangchwisa.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RepositoryMySqlIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 4, 28, 9, 0);
    private static final LocalDateTime COMPLETED_AT = LocalDateTime.of(2026, 4, 28, 9, 30);

    @Autowired
    TrainingSessionRepository trainingSessionRepository;

    @Autowired
    TrainingSessionSummaryRepository trainingSessionSummaryRepository;

    @Autowired
    TrainingProgressRepository trainingProgressRepository;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        cleanMutableTables();
    }

    @Test
    void trainingSessionRepositoryPersistsAndUpdatesSessionAgainstMySql() {
        TrainingSession saved = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                11L,
                STARTED_AT
        ));

        assertThat(saved.sessionId()).isPositive();
        TrainingSession found = trainingSessionRepository.findById(saved.sessionId()).orElseThrow();
        assertThat(found.userId()).isEqualTo(1L);
        assertThat(found.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(found.subType()).isEqualTo("OFFICE");
        assertThat(found.scenarioId()).isEqualTo(11L);
        assertThat(found.status()).isEqualTo(TrainingSessionStatus.IN_PROGRESS);

        trainingSessionRepository.update(found.advanceToStep(2).complete(COMPLETED_AT));

        TrainingSession completed = trainingSessionRepository.findById(saved.sessionId()).orElseThrow();
        assertThat(completed.status()).isEqualTo(TrainingSessionStatus.COMPLETED);
        assertThat(completed.currentStep()).isEqualTo(2);
        assertThat(completed.endedAt()).isEqualTo(COMPLETED_AT);
    }

    @Test
    void summaryRepositoryCountsAndListsSnapshotsByUserAndType() {
        TrainingSession safetySession = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SAFETY,
                null,
                21L,
                STARTED_AT
        ));
        TrainingSession otherSession = trainingSessionRepository.save(TrainingSession.start(
                2L,
                TrainingType.SAFETY,
                null,
                22L,
                STARTED_AT
        ));
        trainingSessionSummaryRepository.save(summary(
                safetySession.sessionId(),
                1L,
                SafetyCategory.COMMUTE_SAFETY,
                COMPLETED_AT
        ));
        trainingSessionSummaryRepository.save(summary(
                otherSession.sessionId(),
                2L,
                SafetyCategory.INFECTIOUS_DISEASE,
                COMPLETED_AT.plusMinutes(1)
        ));

        long count = trainingSessionSummaryRepository.countByUserIdAndTrainingType(
                1L,
                TrainingType.SAFETY
        );
        List<TrainingSessionListItemResponse> sessions = trainingSessionSummaryRepository.findByUserIdAndTrainingType(
                1L,
                TrainingType.SAFETY,
                0,
                10
        );

        assertThat(count).isEqualTo(1L);
        assertThat(sessions).hasSize(1);
        assertThat(sessions.getFirst().sessionId()).isEqualTo(safetySession.sessionId());
        assertThat(sessions.getFirst().category()).isEqualTo(SafetyCategory.COMMUTE_SAFETY);
        assertThat(sessions.getFirst().correctCount()).isEqualTo(8);
        assertThat(sessions.getFirst().totalCount()).isEqualTo(10);
    }

    @Test
    void progressRepositoryFindsMonthlySummariesWithinInclusiveStartAndExclusiveEnd() {
        TrainingSession beforeMonth = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                11L,
                STARTED_AT
        ));
        TrainingSession insideMonth = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                12L,
                STARTED_AT
        ));
        TrainingSession atPeriodEnd = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                13L,
                STARTED_AT
        ));
        trainingSessionSummaryRepository.save(socialSummary(
                beforeMonth.sessionId(),
                LocalDateTime.of(2026, 3, 31, 23, 59, 59)
        ));
        trainingSessionSummaryRepository.save(socialSummary(
                insideMonth.sessionId(),
                LocalDateTime.of(2026, 4, 1, 0, 0)
        ));
        trainingSessionSummaryRepository.save(socialSummary(
                atPeriodEnd.sessionId(),
                LocalDateTime.of(2026, 5, 1, 0, 0)
        ));

        List<MonthlyTrainingSummaryEntry> summaries = trainingProgressRepository.findMonthlyCompletedSummaries(
                1L,
                TrainingType.SOCIAL,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 5, 1, 0, 0)
        );

        assertThat(summaries).hasSize(1);
        assertThat(summaries.getFirst().score()).isEqualTo(85);
        assertThat(summaries.getFirst().sessionSubType()).isEqualTo("OFFICE");
    }

    @Test
    void outboxRepositoryFindsDueEventsAndTransitionsStatus() {
        TrainingSession session = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                11L,
                STARTED_AT
        ));
        OutboxEvent event = outboxEvent(session.sessionId(), "evt-repository-001", "PENDING", 0, null);
        outboxEventRepository.save(event);

        List<OutboxEvent> dueEvents = outboxEventRepository.findPublishableEvents(COMPLETED_AT, 10);

        assertThat(dueEvents).hasSize(1);
        assertThat(dueEvents.getFirst().eventId()).isEqualTo("evt-repository-001");
        assertThat(dueEvents.getFirst().userId()).isEqualTo(1L);
        assertThat(dueEvents.getFirst().payloadJson()).contains("\"sessionId\":" + session.sessionId());

        outboxEventRepository.markRetry("evt-repository-001", 1, COMPLETED_AT.plusMinutes(5), "broker unavailable", COMPLETED_AT);
        assertThat(outboxEventRepository.findPublishableEvents(COMPLETED_AT.plusMinutes(4), 10)).isEmpty();
        assertThat(outboxEventRepository.findPublishableEvents(COMPLETED_AT.plusMinutes(5), 10)).hasSize(1);

        outboxEventRepository.markPublished("evt-repository-001", COMPLETED_AT.plusMinutes(6));
        assertThat(outboxEventRepository.findPublishableEvents(COMPLETED_AT.plusMinutes(10), 10)).isEmpty();
    }

    private TrainingSessionSummary summary(
            long sessionId,
            long userId,
            SafetyCategory category,
            LocalDateTime completedAt
    ) {
        return new TrainingSessionSummary(
                sessionId,
                userId,
                TrainingType.SAFETY,
                21L,
                "Commute safety",
                category.name(),
                "Commute safety",
                80,
                "Safety summary",
                "Handled the scenario safely.",
                8,
                10,
                BigDecimal.valueOf(80),
                2,
                null,
                null,
                completedAt,
                completedAt
        );
    }

    private TrainingSessionSummary socialSummary(long sessionId, LocalDateTime completedAt) {
        return new TrainingSessionSummary(
                sessionId,
                1L,
                TrainingType.SOCIAL,
                11L,
                "Social scenario",
                null,
                "Social training",
                85,
                "Social summary",
                "Good response.",
                null,
                null,
                null,
                null,
                null,
                null,
                completedAt,
                completedAt
        );
    }

    private OutboxEvent outboxEvent(
            long sessionId,
            String eventId,
            String status,
            int retryCount,
            LocalDateTime nextRetryAt
    ) {
        return new OutboxEvent(
                eventId,
                "TrainingCompleted",
                "TrainingSession",
                sessionId,
                sessionId,
                1L,
                TrainingType.SOCIAL,
                """
                {"eventId":"%s","eventType":"TrainingCompleted","userId":1,"sessionId":%d,"trainingType":"SOCIAL","score":85,"scoreType":"AI_EVALUATION","completedAt":"%s"}
                """.formatted(eventId, sessionId, COMPLETED_AT).trim(),
                status,
                retryCount,
                5,
                nextRetryAt,
                null,
                null,
                null,
                COMPLETED_AT.minusMinutes(1),
                COMPLETED_AT.minusMinutes(1)
        );
    }
}
