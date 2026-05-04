package com.didgo.trainingservice.training.document.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.didgo.trainingservice.common.exception.GlobalExceptionHandler;
import com.didgo.trainingservice.common.security.CurrentUserArgumentResolver;
import com.didgo.trainingservice.common.security.TrustedUserHeaderProperties;
import com.didgo.trainingservice.training.document.dto.DocumentAnswerDetailResponse;
import com.didgo.trainingservice.training.document.dto.DocumentQuestionResponse;
import com.didgo.trainingservice.training.document.repository.DocumentTrainingRepository;
import com.didgo.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentFeedbackRow;
import com.didgo.trainingservice.training.document.repository.DocumentTrainingRepository.DocumentQuestionAnswerRow;
import com.didgo.trainingservice.training.document.repository.DocumentTrainingRepository.ScoredDocumentAnswer;
import com.didgo.trainingservice.training.document.service.DocumentTrainingService;
import com.didgo.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.didgo.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.didgo.trainingservice.training.session.repository.TrainingSessionRepository;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.LongStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DocumentTrainingControllerTest {

    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeDocumentTrainingRepository documentRepository = new FakeDocumentTrainingRepository();
    FakeTrainingProgressRepository progressRepository = new FakeTrainingProgressRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository(ownershipRepository);
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        TrainingSessionService trainingSessionService = new TrainingSessionService(
                sessionRepository,
                Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
        );
        DocumentTrainingService documentTrainingService = new DocumentTrainingService(
                documentRepository,
                trainingSessionService,
                new SessionOwnershipValidator(ownershipRepository),
                progressRepository,
                null
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DocumentTrainingController(documentTrainingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }

    @Test
    void startsDocumentSession() throws Exception {
        documentRepository.questions = questions(1L, 5);
        progressRepository.documentProgress = Optional.of(documentProgress(1, 1));

        mockMvc.perform(post("/api/trainings/document/sessions")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.questions.length()").value(5))
                .andExpect(jsonPath("$.data.questions[0].questionId").value(1))
                .andExpect(jsonPath("$.data.questions[0].title").value("Document question 1"))
                .andExpect(jsonPath("$.data.questions[0].questionType").value("SHORT_ANSWER"))
                .andExpect(jsonPath("$.data.questions[0].choices.length()").value(0));
    }

    @Test
    void returnsDocumentProgress() throws Exception {
        progressRepository.documentProgress = Optional.of(documentProgress(2, 3));

        mockMvc.perform(get("/api/trainings/document/progress")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingType").value("DOCUMENT"))
                .andExpect(jsonPath("$.data.currentLevel").value(2))
                .andExpect(jsonPath("$.data.highestUnlockedLevel").value(3))
                .andExpect(jsonPath("$.data.lastPlayedLevel").value(2))
                .andExpect(jsonPath("$.data.lastAccuracyRate").value(80.0));
    }

    @Test
    void rejectsInvalidDocumentLevel() throws Exception {
        mockMvc.perform(post("/api/trainings/document/sessions")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":6}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void returnsSessionDetailAfterOwnershipValidation() throws Exception {
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

        mockMvc.perform(get("/api/trainings/document/sessions/10/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(10))
                .andExpect(jsonPath("$.data.score").value(80))
                .andExpect(jsonPath("$.data.answerSummary.correctCount").value(8))
                .andExpect(jsonPath("$.data.answerSummary.totalCount").value(10))
                .andExpect(jsonPath("$.data.answers[0].questionId").value(1))
                .andExpect(jsonPath("$.data.answers[0].correct").value(true));
    }

    @Test
    void rejectsAnotherUsersDetail() throws Exception {
        ownershipRepository.save(10L, 2L);

        mockMvc.perform(get("/api/trainings/document/sessions/10/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value("Training session belongs to another user."));
    }

    @Test
    void returnsNotFoundWhenSessionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/trainings/document/sessions/999/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Training session was not found."));
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
                .mapToObj(DocumentTrainingControllerTest::question)
                .toList();
    }

    private static DocumentProgressResponse documentProgress(int currentLevel, int highestUnlockedLevel) {
        return new DocumentProgressResponse(
                TrainingType.DOCUMENT,
                10L,
                4,
                5,
                80,
                currentLevel,
                highestUnlockedLevel,
                2,
                BigDecimal.valueOf(80),
                1,
                java.time.LocalDateTime.of(2026, 4, 27, 10, 40)
        );
    }

    static class FakeTrainingProgressRepository implements TrainingProgressRepository {

        Optional<DocumentProgressResponse> documentProgress = Optional.empty();

        @Override
        public Optional<DocumentProgressResponse> findDocumentProgress(long userId) {
            return documentProgress;
        }
    }

    static class FakeDocumentTrainingRepository implements DocumentTrainingRepository {

        List<DocumentQuestionResponse> questions = List.of();
        Optional<DocumentScoreRow> score = Optional.empty();
        Optional<DocumentFeedbackRow> feedback = Optional.empty();
        List<DocumentAnswerDetailResponse> answerLogs = List.of();

        @Override
        public List<DocumentQuestionResponse> findActiveQuestions() {
            return questions;
        }

        @Override
        public List<DocumentQuestionResponse> findRandomActiveQuestionsByDifficulty(String difficulty, int limit) {
            return questions;
        }

        @Override
        public void saveSessionQuestions(long sessionId, List<DocumentQuestionResponse> questions) {
        }

        @Override
        public List<Long> findSessionQuestionIds(long sessionId) {
            return List.of();
        }

        @Override
        public List<DocumentQuestionAnswerRow> findAssignedQuestionAnswers(long sessionId) {
            return List.of();
        }

        @Override
        public void saveAnswerLogs(long sessionId, List<ScoredDocumentAnswer> answers) {
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
