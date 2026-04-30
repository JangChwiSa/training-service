package com.didgo.trainingservice.training.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingSessionStatus;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.repository.TrainingSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TrainingSessionServiceTest {

    static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    static final Instant NOW = Instant.parse("2026-04-28T01:00:00Z");

    FakeTrainingSessionRepository repository = new FakeTrainingSessionRepository();
    TrainingSessionService service = new TrainingSessionService(repository, Clock.fixed(NOW, ZONE_ID));

    @Test
    void createsSocialSession() {
        TrainingSession session = service.createSession(new CreateTrainingSessionCommand(
                1L,
                TrainingType.SOCIAL,
                "OFFICE",
                10L
        ));

        assertThat(session.sessionId()).isEqualTo(1L);
        assertThat(session.userId()).isEqualTo(1L);
        assertThat(session.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(session.subType()).isEqualTo("OFFICE");
        assertThat(session.scenarioId()).isEqualTo(10L);
        assertThat(session.status()).isEqualTo(TrainingSessionStatus.IN_PROGRESS);
        assertThat(session.startedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZONE_ID));
    }

    @Test
    void createsAllTrainingTypesThroughCommonLogic() {
        TrainingSession social = service.createSession(command(1L, TrainingType.SOCIAL, "STORE", 10L));
        TrainingSession safety = service.createSession(command(1L, TrainingType.SAFETY, null, 20L));
        TrainingSession focus = service.createSession(command(1L, TrainingType.FOCUS, "2", null));
        TrainingSession document = service.createSession(command(1L, TrainingType.DOCUMENT, null, null));

        assertThat(social.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(safety.trainingType()).isEqualTo(TrainingType.SAFETY);
        assertThat(focus.trainingType()).isEqualTo(TrainingType.FOCUS);
        assertThat(document.trainingType()).isEqualTo(TrainingType.DOCUMENT);
        assertThat(repository.sessions).hasSize(4);
    }

    @Test
    void rejectsMismatchedCurrentUserWhenCreatingSession() {
        CreateTrainingSessionCommand command = command(2L, TrainingType.SOCIAL, "OFFICE", 10L);

        assertThatThrownBy(() -> service.createSession(new CurrentUser(1L), command))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(exception.getMessage()).isEqualTo("Current user does not match session command user.");
                });
    }

    @Test
    void completesSession() {
        TrainingSession session = service.createSession(command(1L, TrainingType.SOCIAL, "OFFICE", 10L));

        TrainingSession completed = service.completeSession(session.sessionId());

        assertThat(completed.status()).isEqualTo(TrainingSessionStatus.COMPLETED);
        assertThat(completed.endedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZONE_ID));
        assertThat(repository.sessions.get(session.sessionId()).status()).isEqualTo(TrainingSessionStatus.COMPLETED);
    }

    @Test
    void failsSession() {
        TrainingSession session = service.createSession(command(1L, TrainingType.DOCUMENT, null, null));

        TrainingSession failed = service.failSession(session.sessionId());

        assertThat(failed.status()).isEqualTo(TrainingSessionStatus.FAILED);
        assertThat(repository.sessions.get(session.sessionId()).status()).isEqualTo(TrainingSessionStatus.FAILED);
    }

    @Test
    void advancesSessionCurrentStep() {
        TrainingSession session = service.createSession(command(1L, TrainingType.SAFETY, null, 20L));

        TrainingSession advanced = service.advanceCurrentStep(session.sessionId(), 3);

        assertThat(advanced.currentStep()).isEqualTo(3);
        assertThat(repository.sessions.get(session.sessionId()).currentStep()).isEqualTo(3);
    }

    @Test
    void throwsNotFoundWhenSessionDoesNotExist() {
        assertThatThrownBy(() -> service.completeSession(999L))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Training session was not found.");
                });
    }

    private CreateTrainingSessionCommand command(
            long userId,
            TrainingType trainingType,
            String subType,
            Long scenarioId
    ) {
        return new CreateTrainingSessionCommand(userId, trainingType, subType, scenarioId);
    }

    static class FakeTrainingSessionRepository implements TrainingSessionRepository {

        private final Map<Long, TrainingSession> sessions = new HashMap<>();
        private long sequence = 1L;

        @Override
        public TrainingSession save(TrainingSession trainingSession) {
            TrainingSession savedSession = trainingSession.withSessionId(sequence++);
            sessions.put(savedSession.sessionId(), savedSession);
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
}
