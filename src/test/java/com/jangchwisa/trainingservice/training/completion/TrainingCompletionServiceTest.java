package com.jangchwisa.trainingservice.training.completion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;
import com.jangchwisa.trainingservice.event.outbox.OutboxEventRepository;
import com.jangchwisa.trainingservice.training.feedback.entity.TrainingFeedback;
import com.jangchwisa.trainingservice.training.feedback.repository.TrainingFeedbackRepository;
import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.score.entity.TrainingScore;
import com.jangchwisa.trainingservice.training.score.repository.TrainingScoreRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.summary.entity.TrainingSessionSummary;
import com.jangchwisa.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrainingCompletionServiceTest {

    private static final Instant NOW = Instant.parse("2026-04-28T11:00:00Z");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime COMPLETED_AT = LocalDateTime.ofInstant(NOW, ZONE_ID);

    private final FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository();
    private final CapturingTrainingScoreRepository scoreRepository = new CapturingTrainingScoreRepository();
    private final CapturingTrainingFeedbackRepository feedbackRepository = new CapturingTrainingFeedbackRepository();
    private final CapturingTrainingProgressRepository progressRepository = new CapturingTrainingProgressRepository();
    private final CapturingTrainingSessionSummaryRepository summaryRepository = new CapturingTrainingSessionSummaryRepository();
    private final CapturingOutboxEventRepository outboxEventRepository = new CapturingOutboxEventRepository();
    private final TrainingCompletionService service = new TrainingCompletionService(
            sessionRepository,
            scoreRepository,
            feedbackRepository,
            progressRepository,
            summaryRepository,
            outboxEventRepository,
            Clock.fixed(NOW, ZONE_ID)
    );

    @Test
    void completesSessionAndStoresCompletionDataWithOutboxEvent() {
        sessionRepository.session = new TrainingSession(
                10L,
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                20L,
                TrainingSessionStatus.IN_PROGRESS,
                0,
                COMPLETED_AT.minusMinutes(5),
                null
        );
        List<String> originalDataWrites = new ArrayList<>();

        TrainingCompletionResult result = service.complete(new TrainingCompletionCommand(
                1L,
                10L,
                TrainingType.SOCIAL,
                new TrainingCompletionScore(85, "AI_EVALUATION", null, null, null, null, null, "{\"source\":\"ai\"}"),
                new TrainingCompletionFeedback("SUMMARY", "AI", "상황에 맞게 정중하게 대화했습니다.", "상세 피드백"),
                new TrainingCompletionSummary(20L, "동료에게 도움 요청하기", null, "동료에게 도움 요청하기", "사회성 훈련 완료", null, null, null, null, null, null),
                TrainingCompletionProgress.none(),
                () -> originalDataWrites.add("social_dialog_logs")
        ));

        assertThat(result.sessionId()).isEqualTo(10L);
        assertThat(result.score()).isEqualTo(85);
        assertThat(result.completed()).isTrue();
        assertThat(result.completedAt()).isEqualTo(COMPLETED_AT);
        assertThat(originalDataWrites).containsExactly("social_dialog_logs");
        assertThat(scoreRepository.saved.score()).isEqualTo(85);
        assertThat(scoreRepository.saved.createdAt()).isEqualTo(COMPLETED_AT);
        assertThat(feedbackRepository.saved.summary()).isEqualTo("상황에 맞게 정중하게 대화했습니다.");
        assertThat(progressRepository.saved.userId()).isEqualTo(1L);
        assertThat(progressRepository.saved.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(summaryRepository.saved.feedbackSummary()).isEqualTo("상황에 맞게 정중하게 대화했습니다.");
        assertThat(sessionRepository.updated.status()).isEqualTo(TrainingSessionStatus.COMPLETED);
        assertThat(sessionRepository.updated.endedAt()).isEqualTo(COMPLETED_AT);
        assertThat(outboxEventRepository.saved.eventType()).isEqualTo("TrainingCompleted");
        assertThat(outboxEventRepository.saved.payloadJson()).contains("\"sessionId\":10");
        assertThat(outboxEventRepository.saved.payloadJson()).contains("\"trainingType\":\"SOCIAL\"");
    }

    @Test
    void rejectsCompletionWhenSessionBelongsToAnotherUser() {
        sessionRepository.session = new TrainingSession(
                10L,
                2L,
                TrainingType.SAFETY,
                null,
                30L,
                TrainingSessionStatus.IN_PROGRESS,
                0,
                COMPLETED_AT.minusMinutes(3),
                null
        );

        assertThatThrownBy(() -> service.complete(safetyCommand()))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        assertThat(scoreRepository.saved).isNull();
        assertThat(outboxEventRepository.saved).isNull();
    }

    @Test
    void rejectsDuplicateCompletion() {
        sessionRepository.session = new TrainingSession(
                10L,
                1L,
                TrainingType.SAFETY,
                null,
                30L,
                TrainingSessionStatus.COMPLETED,
                0,
                COMPLETED_AT.minusMinutes(3),
                COMPLETED_AT.minusMinutes(1)
        );

        assertThatThrownBy(() -> service.complete(safetyCommand()))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.CONFLICT));
        assertThat(summaryRepository.saved).isNull();
        assertThat(outboxEventRepository.saved).isNull();
    }

    @Test
    void rejectsSafetyCompletionWithoutCounts() {
        sessionRepository.session = new TrainingSession(
                10L,
                1L,
                TrainingType.SAFETY,
                null,
                30L,
                TrainingSessionStatus.IN_PROGRESS,
                0,
                COMPLETED_AT.minusMinutes(3),
                null
        );

        TrainingCompletionCommand command = new TrainingCompletionCommand(
                1L,
                10L,
                TrainingType.SAFETY,
                new TrainingCompletionScore(70, "CHOICE_RESULT", null, null, null, null, null, null),
                new TrainingCompletionFeedback("SUMMARY", "SYSTEM", "안전 훈련 완료", null),
                new TrainingCompletionSummary(30L, "출근길 안전", "COMMUTE_SAFETY", "출근길 안전", null, null, null, null, null, null, null),
                TrainingCompletionProgress.none(),
                null
        );

        assertThatThrownBy(() -> service.complete(command))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception ->
                        assertThat(exception.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    private TrainingCompletionCommand safetyCommand() {
        return new TrainingCompletionCommand(
                1L,
                10L,
                TrainingType.SAFETY,
                new TrainingCompletionScore(70, "CHOICE_RESULT", 7, 10, BigDecimal.valueOf(70), 3, null, null),
                new TrainingCompletionFeedback("SUMMARY", "SYSTEM", "대부분의 위험 상황을 올바르게 판단했습니다.", "상세 피드백"),
                new TrainingCompletionSummary(30L, "출근길 안전", "COMMUTE_SAFETY", "출근길 안전", null, 7, 10, BigDecimal.valueOf(70), 3, null, null),
                TrainingCompletionProgress.none(),
                null
        );
    }

    private static class FakeTrainingSessionRepository implements TrainingSessionRepository {

        private TrainingSession session;
        private TrainingSession updated;

        @Override
        public TrainingSession save(TrainingSession trainingSession) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<TrainingSession> findById(long sessionId) {
            return Optional.ofNullable(session);
        }

        @Override
        public void update(TrainingSession trainingSession) {
            this.updated = trainingSession;
        }
    }

    private static class CapturingTrainingScoreRepository implements TrainingScoreRepository {

        private TrainingScore saved;

        @Override
        public void save(TrainingScore trainingScore) {
            this.saved = trainingScore;
        }
    }

    private static class CapturingTrainingFeedbackRepository implements TrainingFeedbackRepository {

        private TrainingFeedback saved;

        @Override
        public void save(TrainingFeedback trainingFeedback) {
            this.saved = trainingFeedback;
        }
    }

    private static class CapturingTrainingProgressRepository implements TrainingProgressRepository {

        private TrainingProgressCompletion saved;

        @Override
        public void applyCompletion(TrainingProgressCompletion completion) {
            this.saved = completion;
        }

        @Override
        public Optional<SocialProgressResponse> findSocialProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<SafetyProgressResponse> findSafetyProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<DocumentProgressResponse> findDocumentProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<FocusProgressResponse> findFocusProgress(long userId) {
            return Optional.empty();
        }
    }

    private static class CapturingTrainingSessionSummaryRepository implements TrainingSessionSummaryRepository {

        private TrainingSessionSummary saved;

        @Override
        public void save(TrainingSessionSummary summary) {
            this.saved = summary;
        }

        @Override
        public long countByUserIdAndTrainingType(long userId, TrainingType trainingType, SafetyCategory category) {
            return 0;
        }

        @Override
        public List<com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse> findByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType,
                SafetyCategory category,
                int page,
                int size
        ) {
            return List.of();
        }
    }

    private static class CapturingOutboxEventRepository implements OutboxEventRepository {

        private OutboxEvent saved;

        @Override
        public void save(OutboxEvent outboxEvent) {
            this.saved = outboxEvent;
        }
    }
}
