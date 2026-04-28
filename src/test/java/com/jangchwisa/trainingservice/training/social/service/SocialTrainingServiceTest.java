package com.jangchwisa.trainingservice.training.social.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import com.jangchwisa.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.StartSocialSessionResponse;
import com.jangchwisa.trainingservice.training.social.entity.SocialDialogSpeaker;
import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import com.jangchwisa.trainingservice.training.social.repository.SocialTrainingRepository;
import java.time.Clock;
import java.time.Instant;
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
        socialRepository.feedback = Optional.of(new SocialFeedbackResponse("요약", "상세"));
        socialRepository.dialogLogs = List.of(new SocialDialogLogResponse(1, SocialDialogSpeaker.USER, "도와주세요"));

        SocialSessionDetailResponse response = service.getSessionDetail(new CurrentUser(1L), 10L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.score()).isEqualTo(85);
        assertThat(response.feedback().summary()).isEqualTo("요약");
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

    static class FakeSocialTrainingRepository implements SocialTrainingRepository {

        Map<Long, SocialJobType> activeScenarios = new HashMap<>();
        Optional<SocialScoreRow> score = Optional.empty();
        Optional<SocialFeedbackResponse> feedback = Optional.empty();
        List<SocialDialogLogResponse> dialogLogs = List.of();

        @Override
        public List<SocialScenarioListItemResponse> findActiveScenariosByJobType(SocialJobType jobType) {
            return List.of(new SocialScenarioListItemResponse(1L, "동료에게 도움 요청하기", "EASY"));
        }

        @Override
        public Optional<SocialScenarioDetailResponse> findActiveScenarioDetail(long scenarioId) {
            return Optional.of(new SocialScenarioDetailResponse(
                    scenarioId,
                    SocialJobType.OFFICE,
                    "동료에게 도움 요청하기",
                    "배경",
                    "상황",
                    "동료",
                    "EASY"
            ));
        }

        @Override
        public boolean existsActiveScenario(long scenarioId, SocialJobType jobType) {
            return activeScenarios.get(scenarioId) == jobType;
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
}
