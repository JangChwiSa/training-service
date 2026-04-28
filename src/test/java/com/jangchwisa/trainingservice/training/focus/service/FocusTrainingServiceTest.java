package com.jangchwisa.trainingservice.training.focus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.focus.dto.FocusCommandResponse;
import com.jangchwisa.trainingservice.training.focus.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.focus.dto.StartFocusSessionResponse;
import com.jangchwisa.trainingservice.training.focus.repository.FocusTrainingRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FocusTrainingServiceTest {

    FakeFocusTrainingRepository focusRepository = new FakeFocusTrainingRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository();
    FocusTrainingService service = new FocusTrainingService(
            focusRepository,
            new TrainingSessionService(
                    sessionRepository,
                    Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
            ),
            new FocusCommandGenerator()
    );

    @Test
    void returnsDefaultProgressWhenProgressDoesNotExist() {
        FocusProgressResponse response = service.getProgress(1L);

        assertThat(response).isEqualTo(new FocusProgressResponse(1, 1, null, null, null));
    }

    @Test
    void startsFocusSessionAndStoresLevelAsSubType() {
        focusRepository.progress = Optional.of(new FocusProgressResponse(2, 2, 1, BigDecimal.valueOf(90), 700));
        focusRepository.rule = Optional.of(new FocusTrainingRepository.FocusLevelRuleRow(
                2,
                6,
                3000,
                "SIMPLE",
                BigDecimal.valueOf(80)
        ));

        StartFocusSessionResponse response = service.startSession(new CurrentUser(7L), 2);

        TrainingSession savedSession = sessionRepository.sessions.get(response.sessionId());
        assertThat(savedSession.trainingType()).isEqualTo(TrainingType.FOCUS);
        assertThat(savedSession.subType()).isEqualTo("2");
        assertThat(response.durationSeconds()).isEqualTo(6);
        assertThat(response.commands()).hasSize(2);
        assertThat(response.commands().getFirst().order()).isEqualTo(1);
        assertThat(response.commands().getFirst().displayAtMs()).isZero();
        assertThat(focusRepository.savedCommands).hasSize(2);
    }

    @Test
    void rejectsLockedLevel() {
        focusRepository.progress = Optional.of(new FocusProgressResponse(1, 1, null, null, null));
        focusRepository.rule = Optional.of(new FocusTrainingRepository.FocusLevelRuleRow(
                2,
                180,
                3000,
                "SIMPLE",
                BigDecimal.valueOf(80)
        ));

        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L), 2))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(exception.getMessage()).isEqualTo("Focus level is locked.");
                });
    }

    @Test
    void rejectsUnknownLevelRule() {
        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L), 99))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Focus level rule was not found.");
                });
    }

    static class FakeFocusTrainingRepository implements FocusTrainingRepository {

        Optional<FocusProgressResponse> progress = Optional.empty();
        Optional<FocusLevelRuleRow> rule = Optional.empty();
        List<NewFocusCommand> savedCommands = new ArrayList<>();

        @Override
        public Optional<FocusProgressResponse> findProgress(long userId) {
            return progress;
        }

        @Override
        public Optional<FocusLevelRuleRow> findActiveLevelRule(int level) {
            return rule;
        }

        @Override
        public List<FocusCommandResponse> saveCommands(long sessionId, List<NewFocusCommand> commands) {
            savedCommands.addAll(commands);
            List<FocusCommandResponse> responses = new ArrayList<>();
            long commandId = 1000L;
            for (NewFocusCommand command : commands) {
                responses.add(new FocusCommandResponse(
                        commandId++,
                        command.order(),
                        command.commandText(),
                        command.expectedAction(),
                        command.displayAtMs()
                ));
            }
            return responses;
        }
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
