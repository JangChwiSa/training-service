package com.jangchwisa.trainingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.training.completion.TrainingCompletionCommand;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionFeedback;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionProgress;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionResult;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionScore;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionService;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionSummary;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;

class TrainingCompletionServiceMySqlIntegrationTest extends AbstractMySqlIntegrationTest {

    private static final Instant NOW = Instant.parse("2026-04-28T11:00:00Z");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 4, 28, 19, 50);
    private static final LocalDateTime COMPLETED_AT = LocalDateTime.ofInstant(NOW, ZONE_ID);

    @Autowired
    TrainingCompletionService trainingCompletionService;

    @Autowired
    TrainingSessionRepository trainingSessionRepository;

    @BeforeEach
    void setUp() {
        cleanMutableTables();
    }

    @Test
    void completionServicePersistsCompletionAggregateAndOutboxInOneTransaction() {
        TrainingSession session = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                11L,
                STARTED_AT
        ));

        TrainingCompletionResult result = trainingCompletionService.complete(socialCommand(
                session.sessionId(),
                "Handled the workplace conversation well.",
                () -> insertSocialDialogLog(session.sessionId(), "I can help with that.")
        ));

        assertThat(result.sessionId()).isEqualTo(session.sessionId());
        assertThat(result.completed()).isTrue();
        assertThat(result.completedAt()).isEqualTo(COMPLETED_AT);
        assertThat(countRows("social_dialog_logs")).isEqualTo(1);
        assertThat(countRows("training_scores")).isEqualTo(1);
        assertThat(countRows("training_feedbacks")).isEqualTo(1);
        assertThat(countRows("user_social_progress")).isEqualTo(1);
        assertThat(countRows("training_session_summaries")).isEqualTo(1);
        assertThat(countRows("outbox_events")).isEqualTo(1);
        assertThat(queryString("SELECT status FROM training_sessions WHERE session_id = ?", session.sessionId()))
                .isEqualTo(TrainingSessionStatus.COMPLETED.name());
        assertThat(queryTimestamp("SELECT ended_at FROM training_sessions WHERE session_id = ?", session.sessionId()))
                .isEqualTo(Timestamp.valueOf(COMPLETED_AT));
        assertThat(queryString("SELECT status FROM outbox_events WHERE session_id = ?", session.sessionId()))
                .isEqualTo("PENDING");
        assertThat(queryString("SELECT event_type FROM outbox_events WHERE session_id = ?", session.sessionId()))
                .isEqualTo("TrainingCompleted");
    }

    @Test
    void completionServiceRollsBackOriginalLogsAndCompletionDataWhenLaterWriteFails() {
        TrainingSession session = trainingSessionRepository.save(TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                11L,
                STARTED_AT
        ));

        assertThatThrownBy(() -> trainingCompletionService.complete(socialCommand(
                session.sessionId(),
                "x".repeat(501),
                () -> insertSocialDialogLog(session.sessionId(), "This log must roll back.")
        )))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(countRows("social_dialog_logs")).isZero();
        assertThat(countRows("training_scores")).isZero();
        assertThat(countRows("training_feedbacks")).isZero();
        assertThat(countRows("user_social_progress")).isZero();
        assertThat(countRows("training_session_summaries")).isZero();
        assertThat(countRows("outbox_events")).isZero();
        assertThat(queryString("SELECT status FROM training_sessions WHERE session_id = ?", session.sessionId()))
                .isEqualTo(TrainingSessionStatus.IN_PROGRESS.name());
        assertThat(queryTimestamp("SELECT ended_at FROM training_sessions WHERE session_id = ?", session.sessionId()))
                .isNull();
    }

    private TrainingCompletionCommand socialCommand(long sessionId, String feedbackSummary, Runnable originalDataWriter) {
        return new TrainingCompletionCommand(
                1L,
                sessionId,
                TrainingType.SOCIAL,
                new TrainingCompletionScore(85, "AI_EVALUATION", null, null, null, null, null, "{\"source\":\"integration\"}"),
                new TrainingCompletionFeedback("SUMMARY", "AI", feedbackSummary, "Detailed feedback"),
                new TrainingCompletionSummary(11L, "Office conversation", null, "Office conversation", "Completed social training.", null, null, null, null, null, null),
                TrainingCompletionProgress.none(),
                originalDataWriter
        );
    }

    private void insertSocialDialogLog(long sessionId, String content) {
        jdbcTemplate.update(
                """
                INSERT INTO social_dialog_logs (session_id, turn_no, speaker, content, created_at)
                VALUES (?, 1, 'USER', ?, ?)
                """,
                sessionId,
                content,
                Timestamp.valueOf(COMPLETED_AT)
        );
    }

    private int countRows(String table) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return count == null ? 0 : count;
    }

    private String queryString(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, String.class, args);
    }

    private Timestamp queryTimestamp(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Timestamp.class, args);
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        Clock integrationTestClock() {
            return Clock.fixed(NOW, ZONE_ID);
        }
    }
}
