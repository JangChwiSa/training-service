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
import com.jangchwisa.trainingservice.training.document.repository.DocumentTrainingRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
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

        @Override
        public List<DocumentQuestionResponse> findActiveQuestions() {
            return questions;
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
