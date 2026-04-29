package com.jangchwisa.trainingservice.training.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;
import com.jangchwisa.trainingservice.event.outbox.OutboxEventRepository;
import com.jangchwisa.trainingservice.training.completion.TrainingCompletionService;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerRequest;
import com.jangchwisa.trainingservice.training.document.dto.DocumentQuestionResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentSessionDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionRequest;
import com.jangchwisa.trainingservice.training.document.dto.StartDocumentSessionResponse;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersRequest;
import com.jangchwisa.trainingservice.training.document.dto.SubmitDocumentAnswersResponse;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentFeedbackRow;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentQuestionAnswerRow;
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository.ScoredDocumentAnswer;
import com.jangchwisa.trainingservice.training.feedback.entity.TrainingFeedback;
import com.jangchwisa.trainingservice.training.feedback.repository.TrainingFeedbackRepository;
import com.jangchwisa.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.score.entity.TrainingScore;
import com.jangchwisa.trainingservice.training.score.repository.TrainingScoreRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSessionStatus;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import com.jangchwisa.trainingservice.training.summary.entity.TrainingSessionSummary;
import com.jangchwisa.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.LongStream;
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
    void startsDocumentSessionAndReturnsFiveLevelQuestions() {
        documentRepository.questions = questions(1L, 5);

        StartDocumentSessionResponse response = service.startSession(
                new CurrentUser(7L),
                new StartDocumentSessionRequest(3)
        );

        TrainingSession savedSession = sessionRepository.sessions.get(response.sessionId());
        assertThat(savedSession.userId()).isEqualTo(7L);
        assertThat(savedSession.trainingType()).isEqualTo(TrainingType.DOCUMENT);
        assertThat(savedSession.subType()).isEqualTo("LEVEL_3");
        assertThat(savedSession.scenarioId()).isNull();
        assertThat(documentRepository.requestedDifficulty).isEqualTo("LEVEL_3");
        assertThat(documentRepository.requestedLimit).isEqualTo(5);
        assertThat(documentRepository.savedSessionQuestions).hasSize(5);
        assertThat(response.questions()).hasSize(5);
        assertThat(response.questions().getFirst().questionId()).isEqualTo(1L);
    }

    @Test
    void rejectsStartWhenLevelHasFewerThanFiveActiveQuestions() {
        documentRepository.questions = questions(1L, 4);

        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L), new StartDocumentSessionRequest(1)))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.CONFLICT);
                    assertThat(exception.getMessage()).isEqualTo("Not enough active document questions for the requested level.");
                });
    }

    @Test
    void rejectsDuplicateQuestionIdsInAnswerSubmission() {
        ownershipRepository.save(10L, 1L);
        SubmitDocumentAnswersRequest request = new SubmitDocumentAnswersRequest(List.of(
                new DocumentAnswerRequest(1L, "Answer 1"),
                new DocumentAnswerRequest(1L, "Another answer")
        ));

        assertThatThrownBy(() -> service.validateAnswerSubmission(new CurrentUser(1L), 10L, request))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(exception.getMessage()).isEqualTo("Duplicate document answer question id.");
                });
    }

    @Test
    void rejectsAnswerSubmissionWhenQuestionSetDoesNotExactlyMatchAssignment() {
        ownershipRepository.save(10L, 1L);
        documentRepository.assignedQuestionIds = List.of(1L, 2L, 3L, 4L, 5L);
        SubmitDocumentAnswersRequest request = new SubmitDocumentAnswersRequest(List.of(
                new DocumentAnswerRequest(1L, "Answer 1"),
                new DocumentAnswerRequest(2L, "Answer 2"),
                new DocumentAnswerRequest(3L, "Answer 3"),
                new DocumentAnswerRequest(4L, "Answer 4"),
                new DocumentAnswerRequest(99L, "Answer 99")
        ));

        assertThatThrownBy(() -> service.validateAnswerSubmission(new CurrentUser(1L), 10L, request))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
                    assertThat(exception.getMessage()).isEqualTo("Document answers must exactly match assigned session questions.");
                });
    }

    @Test
    void returnsDetailAfterOwnershipValidation() {
        ownershipRepository.save(10L, 1L);
        documentRepository.score = Optional.of(new DocumentTrainingRepository.DocumentScoreRow(80, 8, 10));
        documentRepository.feedback = Optional.of(new DocumentFeedbackRow("Summary", "Detail"));
        documentRepository.answerLogs = List.of(new DocumentAnswerDetailResponse(
                1L,
                "What time does work start?",
                "9:00 AM",
                "9:00 AM",
                true,
                "The document says work starts at 9:00 AM."
        ));

        DocumentSessionDetailResponse response = service.getSessionDetail(new CurrentUser(1L), 10L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.score()).isEqualTo(80);
        assertThat(response.answerSummary().correctCount()).isEqualTo(8);
        assertThat(response.answerSummary().totalCount()).isEqualTo(10);
        assertThat(response.answers()).hasSize(1);
        assertThat(response.answers().getFirst().correct()).isTrue();
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
                "LEVEL_1",
                null,
                java.time.LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(10L);
        sessionRepository.sessions.put(10L, session);
        ownershipRepository.save(10L, 1L);
        documentRepository.assignedQuestionIds = List.of(1L, 2L, 3L, 4L, 5L);
        documentRepository.questionAnswers = questionAnswers(1L, 5);
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
                new SubmitDocumentAnswersRequest(List.of(
                        new DocumentAnswerRequest(1L, "Answer 1"),
                        new DocumentAnswerRequest(2L, "Answer 2"),
                        new DocumentAnswerRequest(3L, "Answer 3"),
                        new DocumentAnswerRequest(4L, "Answer 4"),
                        new DocumentAnswerRequest(5L, "Answer 5")
                ))
        );

        assertThat(response.completed()).isTrue();
        assertThat(response.score()).isEqualTo(100);
        assertThat(response.correctCount()).isEqualTo(5);
        assertThat(response.totalCount()).isEqualTo(5);
        assertThat(documentRepository.savedAnswers).hasSize(5);
        assertThat(scoreRepository.saved.scoreType()).isEqualTo("ACCURACY_RATE");
        assertThat(sessionRepository.sessions.get(10L).status()).isEqualTo(TrainingSessionStatus.COMPLETED);
        assertThat(outboxEventRepository.saved.eventType()).isEqualTo("TrainingCompleted");
    }

    private static DocumentQuestionResponse question(long questionId) {
        return new DocumentQuestionResponse(
                questionId,
                "Document question " + questionId,
                "Document text " + questionId,
                "Question " + questionId,
                "SHORT_ANSWER"
        );
    }

    private static List<DocumentQuestionResponse> questions(long startQuestionId, int count) {
        return LongStream.range(startQuestionId, startQuestionId + count)
                .mapToObj(DocumentTrainingServiceTest::question)
                .toList();
    }

    private static List<DocumentQuestionAnswerRow> questionAnswers(long startQuestionId, int count) {
        return LongStream.range(startQuestionId, startQuestionId + count)
                .mapToObj(questionId -> new DocumentQuestionAnswerRow(
                        questionId,
                        "Document question " + questionId,
                        "Question " + questionId,
                        "Answer " + questionId,
                        "Explanation " + questionId
                ))
                .toList();
    }

    static class FakeDocumentTrainingRepository implements DocumentTrainingRepository {

        List<DocumentQuestionResponse> questions = List.of();
        Optional<DocumentScoreRow> score = Optional.empty();
        Optional<DocumentFeedbackRow> feedback = Optional.empty();
        List<DocumentAnswerDetailResponse> answerLogs = List.of();
        List<DocumentQuestionAnswerRow> questionAnswers = List.of();
        List<ScoredDocumentAnswer> savedAnswers = List.of();
        List<Long> assignedQuestionIds = List.of();
        List<DocumentQuestionResponse> savedSessionQuestions = List.of();
        String requestedDifficulty;
        int requestedLimit;

        @Override
        public List<DocumentQuestionResponse> findActiveQuestions() {
            return questions;
        }

        @Override
        public List<DocumentQuestionResponse> findRandomActiveQuestionsByDifficulty(String difficulty, int limit) {
            requestedDifficulty = difficulty;
            requestedLimit = limit;
            return questions;
        }

        @Override
        public void saveSessionQuestions(long sessionId, List<DocumentQuestionResponse> questions) {
            savedSessionQuestions = questions;
            assignedQuestionIds = questions.stream().map(DocumentQuestionResponse::questionId).toList();
        }

        @Override
        public List<Long> findSessionQuestionIds(long sessionId) {
            return assignedQuestionIds;
        }

        @Override
        public List<DocumentQuestionAnswerRow> findAssignedQuestionAnswers(long sessionId) {
            return questionAnswers;
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
        public Optional<DocumentFeedbackRow> findFeedback(long sessionId) {
            return feedback;
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
        public long countByUserIdAndTrainingType(long userId, TrainingType trainingType) {
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
