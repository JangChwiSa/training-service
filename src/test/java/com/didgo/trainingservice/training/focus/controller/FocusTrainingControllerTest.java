package com.didgo.trainingservice.training.focus.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.didgo.trainingservice.common.exception.GlobalExceptionHandler;
import com.didgo.trainingservice.common.security.CurrentUserArgumentResolver;
import com.didgo.trainingservice.common.security.TrustedUserHeaderProperties;
import com.didgo.trainingservice.training.focus.dto.FocusCommandResponse;
import com.didgo.trainingservice.training.focus.dto.FocusProgressResponse;
import com.didgo.trainingservice.training.focus.repository.FocusTrainingRepository;
import com.didgo.trainingservice.training.focus.service.FocusCommandGenerator;
import com.didgo.trainingservice.training.focus.service.FocusTrainingService;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.repository.TrainingSessionRepository;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FocusTrainingControllerTest {

    FakeFocusTrainingRepository focusRepository = new FakeFocusTrainingRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        FocusTrainingService focusTrainingService = new FocusTrainingService(
                focusRepository,
                new TrainingSessionService(
                        sessionRepository,
                        Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
                ),
                new FocusCommandGenerator()
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FocusTrainingController(focusTrainingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }

    @Test
    void returnsFocusProgress() throws Exception {
        focusRepository.progress = Optional.of(new FocusProgressResponse(3, 3, 2, BigDecimal.valueOf(92.5), 820));

        mockMvc.perform(get("/api/trainings/focus/progress")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentLevel").value(3))
                .andExpect(jsonPath("$.data.highestUnlockedLevel").value(3))
                .andExpect(jsonPath("$.data.lastPlayedLevel").value(2))
                .andExpect(jsonPath("$.data.lastAccuracyRate").value(92.5))
                .andExpect(jsonPath("$.data.lastAverageReactionMs").value(820));
    }

    @Test
    void startsFocusSessionAndReturnsCommands() throws Exception {
        focusRepository.progress = Optional.of(new FocusProgressResponse(2, 2, null, null, null));
        focusRepository.rule = Optional.of(new FocusTrainingRepository.FocusLevelRuleRow(
                2,
                6,
                3000,
                "SIMPLE",
                BigDecimal.valueOf(80)
        ));

        mockMvc.perform(post("/api/trainings/focus/sessions")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.level").value(2))
                .andExpect(jsonPath("$.data.durationSeconds").value(6))
                .andExpect(jsonPath("$.data.commands[0].commandId").value(1000))
                .andExpect(jsonPath("$.data.commands[0].order").value(1))
                .andExpect(jsonPath("$.data.commands[0].expectedAction").value("BLUE_UP"));

        org.assertj.core.api.Assertions.assertThat(sessionRepository.sessions.get(1L).subType()).isEqualTo("2");
    }

    @Test
    void rejectsInvalidStartRequest() throws Exception {
        mockMvc.perform(post("/api/trainings/focus/sessions")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    static class FakeFocusTrainingRepository implements FocusTrainingRepository {

        Optional<FocusProgressResponse> progress = Optional.empty();
        Optional<FocusLevelRuleRow> rule = Optional.empty();

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
