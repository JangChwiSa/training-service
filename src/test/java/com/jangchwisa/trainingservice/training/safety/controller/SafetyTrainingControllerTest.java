package com.jangchwisa.trainingservice.training.safety.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jangchwisa.trainingservice.common.exception.GlobalExceptionHandler;
import com.jangchwisa.trainingservice.common.security.CurrentUserArgumentResolver;
import com.jangchwisa.trainingservice.common.security.TrustedUserHeaderProperties;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyActionDetailResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyActionLogResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyChoiceResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyFeedbackResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySceneResponse;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.safety.repository.SafetyTrainingRepository;
import com.jangchwisa.trainingservice.training.safety.service.SafetyTrainingService;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SafetyTrainingControllerTest {

    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository(ownershipRepository);
    FakeSafetyTrainingRepository safetyRepository = new FakeSafetyTrainingRepository();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        TrainingSessionService trainingSessionService = new TrainingSessionService(
                sessionRepository,
                Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
        );
        SafetyTrainingService safetyTrainingService = new SafetyTrainingService(
                safetyRepository,
                trainingSessionService,
                new SessionOwnershipValidator(ownershipRepository)
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SafetyTrainingController(safetyTrainingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }

    @Test
    void returnsSafetyScenarios() throws Exception {
        mockMvc.perform(get("/api/trainings/safety/scenarios")
                        .param("category", "COMMUTE_SAFETY")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].scenarioId").value(1))
                .andExpect(jsonPath("$.data[0].category").value("COMMUTE_SAFETY"))
                .andExpect(jsonPath("$.data[0].title").value("출근길"));

        org.assertj.core.api.Assertions.assertThat(safetyRepository.category).isEqualTo(SafetyCategory.COMMUTE_SAFETY);
    }

    @Test
    void startsSafetySessionAndReturnsFirstScene() throws Exception {
        safetyRepository.activeScenarioIds.add(1L);
        safetyRepository.firstScenes.put(1L, scene(10L, false));

        mockMvc.perform(post("/api/trainings/safety/sessions")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scenarioId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.scene.sceneId").value(10))
                .andExpect(jsonPath("$.data.scene.choices[0].choiceId").value(2))
                .andExpect(jsonPath("$.data.scene.endScene").value(false));

        org.assertj.core.api.Assertions.assertThat(sessionRepository.sessions.get(1L).trainingType())
                .isEqualTo(TrainingType.SAFETY);
    }

    @Test
    void storesChoiceAndReturnsNextScene() throws Exception {
        ownershipRepository.save(20L, 1L);
        sessionRepository.sessions.put(20L, TrainingSession.start(
                1L,
                TrainingType.SAFETY,
                null,
                1L,
                LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(20L));
        safetyRepository.choices.put("1:2", new SafetyTrainingRepository.SafetyChoiceRow(2L, 1L, 3L, true));
        safetyRepository.scenes.put(3L, scene(3L, false));

        mockMvc.perform(post("/api/trainings/safety/sessions/20/next-scene")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sceneId\":1,\"choiceId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selectedResult.correct").value(true))
                .andExpect(jsonPath("$.data.nextScene.sceneId").value(3));
    }

    @Test
    void returnsDetailAfterOwnershipValidation() throws Exception {
        ownershipRepository.save(20L, 1L);
        safetyRepository.score = Optional.of(new SafetyTrainingRepository.SafetyScoreRow(70, 7, 10));
        safetyRepository.feedback = Optional.of(new SafetyFeedbackResponse(
                "대부분의 위험 상황을 올바르게 판단했습니다.",
                "미끄러운 바닥을 발견했을 때 관리자에게 알린 선택은 적절합니다."
        ));
        safetyRepository.actionDetails = List.of(new SafetyActionDetailResponse(
                1L,
                "작업장 바닥에 물이 흘러 있습니다.",
                "관리자에게 알린다",
                true
        ));

        mockMvc.perform(get("/api/trainings/safety/sessions/20/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(20))
                .andExpect(jsonPath("$.data.score").value(70))
                .andExpect(jsonPath("$.data.choiceSummary.correctCount").value(7))
                .andExpect(jsonPath("$.data.choiceSummary.totalCount").value(10))
                .andExpect(jsonPath("$.data.actions[0].sceneId").value(1))
                .andExpect(jsonPath("$.data.actions[0].situationText").value("작업장 바닥에 물이 흘러 있습니다."))
                .andExpect(jsonPath("$.data.actions[0].selectedChoice").value("관리자에게 알린다"))
                .andExpect(jsonPath("$.data.actions[0].correct").value(true))
                .andExpect(jsonPath("$.data.feedback.summary").value("대부분의 위험 상황을 올바르게 판단했습니다."))
                .andExpect(jsonPath("$.data.feedback.detailText").value("미끄러운 바닥을 발견했을 때 관리자에게 알린 선택은 적절합니다."));
    }

    @Test
    void rejectsAnotherUsersSessionDetail() throws Exception {
        ownershipRepository.save(20L, 2L);

        mockMvc.perform(get("/api/trainings/safety/sessions/20/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value("Training session belongs to another user."));
    }

    @Test
    void returnsNotFoundWhenSessionDoesNotExistForNextScene() throws Exception {
        mockMvc.perform(post("/api/trainings/safety/sessions/999/next-scene")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sceneId\":1,\"choiceId\":2}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Training session was not found."));
    }

    @Test
    void rejectsInvalidCategory() throws Exception {
        mockMvc.perform(get("/api/trainings/safety/scenarios")
                        .param("category", "UNKNOWN")
                        .header("X-User-Id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Safety category is invalid."));
    }

    private SafetySceneResponse scene(long sceneId, boolean endScene) {
        return new SafetySceneResponse(
                sceneId,
                "screen",
                "situation",
                "question",
                List.of(new SafetyChoiceResponse(2L, "관리자에게 알린다")),
                endScene
        );
    }

    static class FakeSafetyTrainingRepository implements SafetyTrainingRepository {

        List<Long> activeScenarioIds = new ArrayList<>();
        Map<Long, SafetySceneResponse> firstScenes = new HashMap<>();
        Map<Long, SafetySceneResponse> scenes = new HashMap<>();
        Map<String, SafetyChoiceRow> choices = new HashMap<>();
        List<SafetyActionLogResponse> savedActionLogs = new ArrayList<>();
        Optional<SafetyScoreRow> score = Optional.empty();
        Optional<SafetyFeedbackResponse> feedback = Optional.empty();
        List<SafetyActionDetailResponse> actionDetails = List.of();
        SafetyCategory category;

        @Override
        public List<SafetyScenarioListItemResponse> findActiveScenarios(SafetyCategory category) {
            this.category = category;
            return List.of(new SafetyScenarioListItemResponse(1L, SafetyCategory.COMMUTE_SAFETY, "출근길", "설명"));
        }

        @Override
        public boolean existsActiveScenario(long scenarioId) {
            return activeScenarioIds.contains(scenarioId);
        }

        @Override
        public Optional<SafetySceneResponse> findFirstScene(long scenarioId) {
            return Optional.ofNullable(firstScenes.get(scenarioId));
        }

        @Override
        public Optional<SafetySceneResponse> findScene(long sceneId) {
            return Optional.ofNullable(scenes.get(sceneId));
        }

        @Override
        public List<SafetyChoiceResponse> findChoices(long sceneId) {
            return List.of(new SafetyChoiceResponse(2L, "관리자에게 알린다"));
        }

        @Override
        public Optional<SafetyChoiceRow> findChoice(long sceneId, long choiceId) {
            return Optional.ofNullable(choices.get(sceneId + ":" + choiceId));
        }

        @Override
        public void saveActionLog(long sessionId, long sceneId, long choiceId, boolean correct) {
            savedActionLogs.add(new SafetyActionLogResponse(sceneId, choiceId, correct));
        }

        @Override
        public List<SafetyActionLogResponse> findActionLogs(long sessionId) {
            return savedActionLogs;
        }

        @Override
        public Optional<SafetyScoreRow> findScore(long sessionId) {
            return score;
        }

        @Override
        public Optional<SafetyFeedbackResponse> findFeedback(long sessionId) {
            return feedback;
        }

        @Override
        public List<SafetyActionDetailResponse> findActionDetails(long sessionId) {
            return actionDetails;
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
