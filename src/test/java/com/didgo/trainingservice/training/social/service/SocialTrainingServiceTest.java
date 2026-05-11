package com.didgo.trainingservice.training.social.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.event.outbox.OutboxEvent;
import com.didgo.trainingservice.event.outbox.OutboxEventRepository;
import com.didgo.trainingservice.external.openai.OpenAiAdapterException;
import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.didgo.trainingservice.external.openai.TrainingEvaluationAdapter;
import com.didgo.trainingservice.external.openai.TrainingEvaluationResultMapper;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.didgo.trainingservice.training.completion.TrainingCompletionService;
import com.didgo.trainingservice.training.feedback.entity.TrainingFeedback;
import com.didgo.trainingservice.training.feedback.repository.TrainingFeedbackRepository;
import com.didgo.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.didgo.trainingservice.training.progress.dto.FocusProgressResponse;
import com.didgo.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.didgo.trainingservice.training.progress.dto.SocialProgressResponse;
import com.didgo.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.didgo.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.didgo.trainingservice.training.score.entity.TrainingScore;
import com.didgo.trainingservice.training.score.repository.TrainingScoreRepository;
import com.didgo.trainingservice.training.evaluation.TrainingEvaluationService;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingSessionStatus;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.didgo.trainingservice.training.session.repository.TrainingSessionRepository;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionRequest;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionResponse;
import com.didgo.trainingservice.training.social.dto.SocialDialogLogRequest;
import com.didgo.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.didgo.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.didgo.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.didgo.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.didgo.trainingservice.training.summary.entity.TrainingSessionSummary;
import com.didgo.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import com.didgo.trainingservice.training.social.entity.SocialDialogSpeaker;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class SocialTrainingServiceTest {

    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository(ownershipRepository);
    FakeSocialTrainingRepository socialRepository = new FakeSocialTrainingRepository();
    TrainingSessionService trainingSessionService = new TrainingSessionService(
            sessionRepository,
            Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
    );
    SocialTrainingService service = new SocialTrainingService(
            socialRepository,
            trainingSessionService,
            new SessionOwnershipValidator(ownershipRepository)
    );

    @Test
    void startsSocialSessionWithJobTypeAsSubType() {
        socialRepository.activeScenarios.put(1L, SocialJobType.OFFICE);

        StartSocialSessionResponse response = service.startSession(new CurrentUser(7L), SocialJobType.OFFICE, 1L);

        TrainingSession savedSession = sessionRepository.sessions.get(response.sessionId());
        assertThat(response.scenarioId()).isEqualTo(1L);
        assertThat(savedSession.userId()).isEqualTo(7L);
        assertThat(savedSession.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(savedSession.subType()).isEqualTo("OFFICE");
        assertThat(savedSession.scenarioId()).isEqualTo(1L);
    }

    @Test
    void rejectsStartWhenScenarioDoesNotMatchJobType() {
        socialRepository.activeScenarios.put(1L, SocialJobType.LABOR);

        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L), SocialJobType.OFFICE, 1L))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Social scenario was not found.");
                });
    }

    @Test
    void validatesOwnershipWhenReadingDetail() {
        ownershipRepository.save(10L, 1L);
        socialRepository.score = Optional.of(new SocialTrainingRepository.SocialScoreRow(85, "AI_EVALUATION"));
        socialRepository.feedback = Optional.of(new SocialFeedbackResponse("Summary", "Detail"));
        socialRepository.dialogLogs = List.of(new SocialDialogLogResponse(1, SocialDialogSpeaker.USER, "Hello"));

        SocialSessionDetailResponse response = service.getSessionDetail(new CurrentUser(1L), 10L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.score()).isEqualTo(85);
        assertThat(response.feedback().summary()).isEqualTo("Summary");
        assertThat(response.dialogLogs()).hasSize(1);
    }

    @Test
    void rejectsAnotherUsersDetail() {
        ownershipRepository.save(10L, 2L);

        assertThatThrownBy(() -> service.getSessionDetail(new CurrentUser(1L), 10L))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(exception.getMessage()).isEqualTo("Training session belongs to another user.");
                });
    }

    @Test
    void rejectsAnotherUsersCompletionRequestBeforeEvaluationRuns() {
        RecordingTrainingEvaluationAdapter adapter = new RecordingTrainingEvaluationAdapter();
        SocialTrainingService completionEnabledService = completionEnabledService(adapter);
        ownershipRepository.save(20L, 2L);
        sessionRepository.sessions.put(20L, TrainingSession.start(
                2L,
                TrainingType.SOCIAL,
                "OFFICE",
                1L,
                LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(20L));
        socialRepository.scenarioSummary = Optional.of(new SocialTrainingRepository.SocialScenarioSummaryRow(1L, "Office conversation"));

        assertThatThrownBy(() -> completionEnabledService.completeSession(
                new CurrentUser(1L),
                20L,
                socialCompletionRequest()
        ))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(exception.getMessage()).isEqualTo("Training session belongs to another user.");
                });

        assertThat(adapter.calls).isZero();
        assertThat(socialRepository.savedDialogLogs).isEmpty();
    }

    @Test
    void rejectsCompletionWithoutUserDialogBeforeEvaluationRuns() {
        RecordingTrainingEvaluationAdapter adapter = new RecordingTrainingEvaluationAdapter();
        SocialTrainingService completionEnabledService = completionEnabledService(adapter);
        ownershipRepository.save(30L, 1L);
        sessionRepository.sessions.put(30L, TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                1L,
                LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(30L));
        socialRepository.scenarioSummary = Optional.of(new SocialTrainingRepository.SocialScenarioSummaryRow(1L, "Office conversation"));

        assertThatThrownBy(() -> completionEnabledService.completeSession(
                new CurrentUser(1L),
                30L,
                new CompleteSocialSessionRequest(List.of(
                        new SocialDialogLogRequest(1, SocialDialogSpeaker.AI, "How can I help?")
                ))
        ))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(exception.getMessage()).isEqualTo("사용자 대화가 없어 피드백을 생성할 수 없습니다.");
                });

        assertThat(adapter.calls).isZero();
        assertThat(socialRepository.savedDialogLogs).isEmpty();
    }

    @Test
    void completesWithFallbackFeedbackWhenOpenAiEvaluationFails() {
        RecordingTrainingEvaluationAdapter adapter = new RecordingTrainingEvaluationAdapter();
        adapter.failure = new OpenAiAdapterException("temporary 5xx");
        CapturingTrainingScoreRepository scoreRepository = new CapturingTrainingScoreRepository();
        CapturingTrainingFeedbackRepository feedbackRepository = new CapturingTrainingFeedbackRepository();
        CapturingOutboxEventRepository outboxEventRepository = new CapturingOutboxEventRepository();
        SocialTrainingService completionEnabledService = completionEnabledService(
                adapter,
                scoreRepository,
                feedbackRepository,
                outboxEventRepository
        );
        ownershipRepository.save(30L, 1L);
        sessionRepository.sessions.put(30L, TrainingSession.start(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                1L,
                LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(30L));
        socialRepository.scenarioSummary = Optional.of(new SocialTrainingRepository.SocialScenarioSummaryRow(1L, "Office conversation"));

        CompleteSocialSessionResponse response = completionEnabledService.completeSession(
                new CurrentUser(1L),
                30L,
                socialCompletionRequest()
        );

        assertThat(adapter.calls).isEqualTo(2);
        assertThat(response.completed()).isTrue();
        assertThat(response.score()).isZero();
        assertThat(feedbackRepository.saved.feedbackSource()).isEqualTo("SYSTEM");
        assertThat(scoreRepository.saved.rawMetricsJson()).contains("\"mode\":\"openai_failure\"");
        assertThat(scoreRepository.saved.rawMetricsJson()).contains("temporary 5xx");
        assertThat(socialRepository.savedDialogLogs).hasSize(2);
        assertThat(sessionRepository.sessions.get(30L).status()).isEqualTo(TrainingSessionStatus.COMPLETED);
        assertThat(outboxEventRepository.saved.eventType()).isEqualTo("TrainingCompleted");
    }

    private SocialTrainingService completionEnabledService(RecordingTrainingEvaluationAdapter adapter) {
        return completionEnabledService(
                adapter,
                new CapturingTrainingScoreRepository(),
                new CapturingTrainingFeedbackRepository(),
                new CapturingOutboxEventRepository()
        );
    }

    private SocialTrainingService completionEnabledService(
            RecordingTrainingEvaluationAdapter adapter,
            CapturingTrainingScoreRepository scoreRepository,
            CapturingTrainingFeedbackRepository feedbackRepository,
            CapturingOutboxEventRepository outboxEventRepository
    ) {
        TrainingEvaluationService evaluationService = new TrainingEvaluationService(
                adapter,
                new TrainingEvaluationResultMapper(),
                new OpenAiProperties(null, 50, "test", null, null)
        );
        TrainingCompletionService completionService = new TrainingCompletionService(
                sessionRepository,
                scoreRepository,
                feedbackRepository,
                new NoOpTrainingProgressRepository(),
                new NoOpTrainingSessionSummaryRepository(),
                outboxEventRepository,
                Clock.fixed(Instant.parse("2026-04-28T01:30:00Z"), ZoneId.of("Asia/Seoul"))
        );
        return new SocialTrainingService(
                socialRepository,
                trainingSessionService,
                new SessionOwnershipValidator(ownershipRepository),
                evaluationService,
                completionService
        );
    }

    private CompleteSocialSessionRequest socialCompletionRequest() {
        return new CompleteSocialSessionRequest(List.of(
                new SocialDialogLogRequest(1, SocialDialogSpeaker.USER, "Hello"),
                new SocialDialogLogRequest(2, SocialDialogSpeaker.AI, "How can I help?")
        ));
    }

    static class FakeSocialTrainingRepository implements SocialTrainingRepository {

        Map<Long, SocialJobType> activeScenarios = new HashMap<>();
        Optional<SocialScoreRow> score = Optional.empty();
        Optional<SocialFeedbackResponse> feedback = Optional.empty();
        List<SocialDialogLogResponse> dialogLogs = List.of();
        Optional<SocialScenarioSummaryRow> scenarioSummary = Optional.empty();
        List<SocialDialogLogRequest> savedDialogLogs = List.of();

        @Override
        public List<SocialScenarioListItemResponse> findActiveScenariosByJobType(SocialJobType jobType) {
            return List.of(new SocialScenarioListItemResponse(1L, "Offer help to a coworker", 1));
        }

        @Override
        public Optional<SocialScenarioDetailResponse> findActiveScenarioDetail(long scenarioId) {
            return Optional.of(new SocialScenarioDetailResponse(
                    scenarioId,
                    SocialJobType.OFFICE,
                    "Offer help to a coworker",
                    "Office break room",
                    "A coworker looks upset.",
                    "How would you respond?",
                    1
            ));
        }

        @Override
        public boolean existsActiveScenario(long scenarioId, SocialJobType jobType) {
            return activeScenarios.get(scenarioId) == jobType;
        }

        @Override
        public Optional<SocialScenarioSummaryRow> findScenarioSummaryBySessionId(long sessionId) {
            return scenarioSummary;
        }

        @Override
        public void saveDialogLogs(long sessionId, List<SocialDialogLogRequest> dialogLogs) {
            savedDialogLogs = List.copyOf(dialogLogs);
        }

        @Override
        public Optional<SocialScoreRow> findScore(long sessionId) {
            return score;
        }

        @Override
        public Optional<SocialFeedbackResponse> findFeedback(long sessionId) {
            return feedback;
        }

        @Override
        public List<SocialDialogLogResponse> findDialogLogs(long sessionId) {
            return dialogLogs;
        }
    }

    static class FakeTrainingSessionRepository implements TrainingSessionRepository {

        private final FakeOwnershipRepository ownershipRepository;
        private final Map<Long, TrainingSession> sessions = new HashMap<>();
        private long sequence = 1L;

        FakeTrainingSessionRepository(FakeOwnershipRepository ownershipRepository) {
            this.ownershipRepository = ownershipRepository;
        }

        @Override
        public TrainingSession save(TrainingSession trainingSession) {
            TrainingSession savedSession = trainingSession.withSessionId(sequence++);
            sessions.put(savedSession.sessionId(), savedSession);
            ownershipRepository.save(savedSession.sessionId(), savedSession.userId());
            return savedSession;
        }

        @Override
        public Optional<TrainingSession> findById(long sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }

        @Override
        public void update(TrainingSession trainingSession) {
            sessions.put(trainingSession.sessionId(), trainingSession);
        }
    }

    static class FakeOwnershipRepository implements TrainingSessionOwnershipRepository {

        private final Map<Long, Long> ownerUserIds = new HashMap<>();

        void save(long sessionId, long userId) {
            ownerUserIds.put(sessionId, userId);
        }

        @Override
        public OptionalLong findUserIdBySessionId(long sessionId) {
            Long userId = ownerUserIds.get(sessionId);
            if (userId == null) {
                return OptionalLong.empty();
            }
            return OptionalLong.of(userId);
        }
    }

    private static class RecordingTrainingEvaluationAdapter implements TrainingEvaluationAdapter {

        private int calls;
        private RuntimeException failure;

        @Override
        public TrainingEvaluationResult evaluate(TrainingEvaluationRequest request) {
            calls++;
            if (failure != null) {
                throw failure;
            }
            return new TrainingEvaluationResult(
                    88,
                    "AI_EVALUATION",
                    new com.didgo.trainingservice.external.openai.dto.TrainingEvaluationFeedback("AI summary", "AI detail"),
                    "{\"adapter\":\"test\",\"fallback\":false}"
            );
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

    private static class CapturingOutboxEventRepository implements OutboxEventRepository {

        private OutboxEvent saved;

        @Override
        public void save(OutboxEvent outboxEvent) {
            this.saved = outboxEvent;
        }
    }

    private static class NoOpTrainingProgressRepository implements TrainingProgressRepository {

        @Override
        public void applyCompletion(TrainingProgressCompletion completion) {
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

    private static class NoOpTrainingSessionSummaryRepository implements TrainingSessionSummaryRepository {

        @Override
        public void save(TrainingSessionSummary summary) {
        }

        @Override
        public long countByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType
        ) {
            return 0;
        }

        @Override
        public List<com.didgo.trainingservice.training.summary.dto.TrainingSessionListItemResponse> findByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType,
                int page,
                int size
        ) {
            return List.of();
        }
    }
}
