package com.jangchwisa.trainingservice.training.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerRequest;
import com.jangchwisa.trainingservice.training.document.dto.DocumentQuestionResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentSessionDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionResponse;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersRequest;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersResponse;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentQuestionAnswerRow;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.ScoredDocumentAnswer;
import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;
import com.jangchwisa.trainingservice.event.outbox.OutboxEventRepository;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionService;
import com.jangchwisa.trainingservice.training.feedback.entity.TrainingFeedback;
import com.jangchwisa.trainingservice.training.feedback.repository.TrainingFeedbackRepository;
import com.jangchwisa.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.score.entity.TrainingScore;
import com.jangchwisa.trainingservice.training.score.repository.TrainingScoreRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.entity.TrainingSessionSummary;
import com.jangchwisa.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class DocumentTrainingServiceTest {

    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository(ownershipRepository);
    FakeDocumentTrainingRepository documentRepository = new FakeDocumentTrainingRepository();
    TrainingSessionService trainingSessionService = new TrainingSessionService(
            sessionRepository,
            Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
    );
    DocumentTrainingService service = new DocumentTrainingService(
            documentRepository,
            trainingSessionService,
            new SessionOwnershipValidator(ownershipRepository)
    );

    @Test
    void startsDocumentSessionAndReturnsActiveQuestions() {
        documentRepository.questions = List.of(question(1L));

        StartDocumentSessionResponse response = service.startSession(new CurrentUser(7L));

        TrainingSession savedSession = sessionRepository.sessions.get(response.sessionId());
        assertThat(savedSession.userId()).isEqualTo(7L);
        assertThat(savedSession.trainingType()).isEqualTo(TrainingType.DOCUMENT);
        assertThat(savedSession.subType()).isNull();
        assertThat(savedSession.scenarioId()).isNull();
        assertThat(response.questions()).hasSize(1);
        assertThat(response.questions().get(0).questionId()).isEqualTo(1L);
    }

    @Test
    void rejectsStartWhenActiveQuestionDoesNotExist() {
        documentRepository.questions = List.of();

        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L)))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Document question was not found.");
                });
    }

    @Test
    void rejectsDuplicateQuestionIdsInAnswerSubmission() {
        ownershipRepository.save(10L, 1L);
        SubmitDocumentAnswersRequest request = new SubmitDocumentAnswersRequest(List.of(
                new DocumentAnswerRequest(1L, "오전 9시"),
                new DocumentAnswerRequest(1L, "다른 답변")
        ));

        assertThatThrownBy(() -> service.validateAnswerSubmission(new CurrentUser(1L), 10L, request))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(exception.getMessage()).isEqualTo("Duplicate document answer question id.");
                });
    }

    @Test
    void returnsDetailAfterOwnershipValidation() {
        ownershipRepository.save(10L, 1L);
        documentRepository.score = Optional.of(new DocumentTrainingRepository.DocumentScoreRow(80, 8, 10));
        documentRepository.answerLogs = List.of(new DocumentAnswerDetailResponse(
                1L,
                "변경된 근무 시작 시간은 언제인가요?",
                "오전 9시",
                "오전 9시",
                true,
                "문서에 오전 9시로 변경된다고 명시되어 있습니다."
        ));

        DocumentSessionDetailResponse response = service.getSessionDetail(new CurrentUser(1L), 10L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.score()).isEqualTo(80);
        assertThat(response.answerSummary().correctCount()).isEqualTo(8);
        assertThat(response.answerSummary().totalCount()).isEqualTo(10);
        assertThat(response.answers()).hasSize(1);
        assertThat(response.answers().get(0).correct()).isTrue();
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
    void submitsAnswersAndCompletesDocumentSession() {
        TrainingSession session = TrainingSession.start(
                1L,
                TrainingType.DOCUMENT,
                null,
                null,
                java.time.LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(10L);
        sessionRepository.sessions.put(10L, session);
        ownershipRepository.save(10L, 1L);
        documentRepository.questionAnswers = List.of(new DocumentQuestionAnswerRow(
                1L,
                "근무 시간 변경 안내",
                "변경된 근무 시작 시간은 언제인가요?",
                "오전 9시",
                "문서에 오전 9시로 명시되어 있습니다."
        ));
        CapturingTrainingScoreRepository scoreRepository = new CapturingTrainingScoreRepository();
        CapturingOutboxEventRepository outboxEventRepository = new CapturingOutboxEventRepository();
        DocumentTrainingService completionEnabledService = new DocumentTrainingService(
                documentRepository,
                trainingSessionService,
                new SessionOwnershipValidator(ownershipRepository),
                new TrainingCompletionService(
                        sessionRepository,
                        scoreRepository,
                        new NoOpTrainingFeedbackRepository(),
                        new NoOpTrainingProgressRepository(),
                        new NoOpTrainingSessionSummaryRepository(),
                        outboxEventRepository,
                        Clock.fixed(Instant.parse("2026-04-28T01:30:00Z"), ZoneId.of("Asia/Seoul"))
                )
        );

        SubmitDocumentAnswersResponse response = completionEnabledService.submitAnswers(
                new CurrentUser(1L),
                10L,
                new SubmitDocumentAnswersRequest(List.of(new DocumentAnswerRequest(1L, "오전 9시")))
        );

        assertThat(response.completed()).isTrue();
        assertThat(response.score()).isEqualTo(100);
        assertThat(response.correctCount()).isEqualTo(1);
        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(documentRepository.savedAnswers).hasSize(1);
        assertThat(scoreRepository.saved.scoreType()).isEqualTo("ACCURACY_RATE");
        assertThat(sessionRepository.sessions.get(10L).status()).isEqualTo(TrainingSessionStatus.COMPLETED);
        assertThat(outboxEventRepository.saved.eventType()).isEqualTo("TrainingCompleted");
    }

    private static DocumentQuestionResponse question(long questionId) {
        return new DocumentQuestionResponse(
                questionId,
                "근무 시간 변경 안내",
                "오늘부터 근무 시간이 오전 9시로 변경됩니다.",
                "변경된 근무 시작 시간은 언제인가요?",
                "SHORT_ANSWER"
        );
    }

    static class FakeDocumentTrainingRepository implements DocumentTrainingRepository {

        List<DocumentQuestionResponse> questions = List.of();
        Optional<DocumentScoreRow> score = Optional.empty();
        List<DocumentAnswerDetailResponse> answerLogs = List.of();
        List<DocumentQuestionAnswerRow> questionAnswers = List.of();
        List<ScoredDocumentAnswer> savedAnswers = List.of();

        @Override
        public List<DocumentQuestionResponse> findActiveQuestions() {
            return questions;
        }

        @Override
        public List<DocumentQuestionAnswerRow> findQuestionAnswers(List<Long> questionIds) {
            return questionAnswers;
        }

        @Override
        public void saveAnswerLogs(long sessionId, List<ScoredDocumentAnswer> answers) {
            savedAnswers = answers;
        }

        @Override
        public Optional<DocumentScoreRow> findScore(long sessionId) {
            return score;
        }

        @Override
        public List<DocumentAnswerDetailResponse> findAnswerLogs(long sessionId) {
            return answerLogs;
        }
    }

    private static class CapturingTrainingScoreRepository implements TrainingScoreRepository {

        private TrainingScore saved;

        @Override
        public void save(TrainingScore trainingScore) {
            this.saved = trainingScore;
        }
    }

    private static class CapturingOutboxEventRepository implements OutboxEventRepository {

        private OutboxEvent saved;

        @Override
        public void save(OutboxEvent outboxEvent) {
            this.saved = outboxEvent;
        }
    }

    private static class NoOpTrainingFeedbackRepository implements TrainingFeedbackRepository {

        @Override
        public void save(TrainingFeedback trainingFeedback) {
        }
    }

    private static class NoOpTrainingProgressRepository implements TrainingProgressRepository {

        @Override
        public void applyCompletion(TrainingProgressCompletion completion) {
        }

        @Override
        public Optional<com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse> findSocialProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse> findSafetyProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse> findDocumentProgress(long userId) {
            return Optional.empty();
        }

        @Override
        public Optional<com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse> findFocusProgress(long userId) {
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
        public List<com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse> findByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType,
                int page,
                int size
        ) {
            return List.of();
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
}
