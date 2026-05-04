package com.didgo.trainingservice.training.safety.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.safety.dto.NextSafetySceneResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyActionDetailResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyActionLogResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyChoiceResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyFeedbackResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.didgo.trainingservice.training.safety.dto.SafetySceneResponse;
import com.didgo.trainingservice.training.safety.dto.StartSafetySessionResponse;
import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
import com.didgo.trainingservice.training.safety.repository.SafetyTrainingRepository;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.didgo.trainingservice.training.session.repository.TrainingSessionRepository;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;

class SafetyTrainingServiceTest {

    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository(ownershipRepository);
    FakeSafetyTrainingRepository safetyRepository = new FakeSafetyTrainingRepository();
    TrainingSessionService trainingSessionService = new TrainingSessionService(
            sessionRepository,
            Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
    );
    SafetyTrainingService service = new SafetyTrainingService(
            safetyRepository,
            trainingSessionService,
            new SessionOwnershipValidator(ownershipRepository)
    );

    @Test
    void startsSafetySessionAndReturnsFirstScene() {
        safetyRepository.activeScenarioIds.add(1L);
        safetyRepository.firstScenes.put(1L, scene(10L, false));

        StartSafetySessionResponse response = service.startSession(new CurrentUser(7L), 1L);

        TrainingSession savedSession = sessionRepository.sessions.get(response.sessionId());
        assertThat(savedSession.userId()).isEqualTo(7L);
        assertThat(savedSession.trainingType()).isEqualTo(TrainingType.SAFETY);
        assertThat(savedSession.scenarioId()).isEqualTo(1L);
        assertThat(response.scene().sceneId()).isEqualTo(10L);
        assertThat(response.scene().choices()).hasSize(1);
    }

    @Test
    void rejectsUnknownScenarioWhenStartingSession() {
        assertThatThrownBy(() -> service.startSession(new CurrentUser(7L), 999L))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("Safety scenario was not found.");
                });
    }

    @Test
    void savesActionLogAndReturnsNextScene() {
        ownershipRepository.save(20L, 1L);
        sessionRepository.sessions.put(20L, TrainingSession.start(
                1L,
                TrainingType.SAFETY,
                null,
                1L,
                java.time.LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(20L));
        safetyRepository.choices.put("1:2", new SafetyTrainingRepository.SafetyChoiceRow(
                2L,
                1L,
                3L,
                true,
                "Good choice",
                "Move safely"
        ));
        safetyRepository.scenes.put(3L, scene(3L, false));

        NextSafetySceneResponse response = service.nextScene(new CurrentUser(1L), 20L, 1L, 2L);

        assertThat(response.completed()).isFalse();
        assertThat(response.result()).isNull();
        assertThat(response.nextScene().sceneId()).isEqualTo(3L);
        assertThat(safetyRepository.savedActionLogs).containsExactly(new SafetyActionLogResponse(1L, 2L, true));
        assertThat(sessionRepository.sessions.get(20L).currentStep()).isEqualTo(3);
    }

    @Test
    void returnsCompletionResultWhenNextSceneIsEndScene() {
        ownershipRepository.save(20L, 1L);
        sessionRepository.sessions.put(20L, TrainingSession.start(
                1L,
                TrainingType.SAFETY,
                null,
                1L,
                java.time.LocalDateTime.of(2026, 4, 28, 10, 0)
        ).withSessionId(20L));
        safetyRepository.choices.put("1:2", new SafetyTrainingRepository.SafetyChoiceRow(
                2L,
                1L,
                3L,
                true,
                "Good choice",
                "Move safely",
                "/feedback.png",
                "Feedback alt"
        ));
        safetyRepository.scenes.put(3L, scene(3L, true));

        NextSafetySceneResponse response = service.nextScene(new CurrentUser(1L), 20L, 1L, 2L);

        assertThat(response.completed()).isTrue();
        assertThat(response.nextScene()).isNull();
        assertThat(response.result()).isNotNull();
        assertThat(response.result().correct()).isTrue();
        assertThat(response.result().resultText()).isEqualTo("Good choice");
        assertThat(response.result().effectText()).isEqualTo("Move safely");
        assertThat(response.result().feedbackImageUrl()).isEqualTo("/feedback.png");
        assertThat(response.result().feedbackImageAlt()).isEqualTo("Feedback alt");
    }

    @Test
    void rejectsAnotherUsersNextSceneRequest() {
        ownershipRepository.save(20L, 2L);

        assertThatThrownBy(() -> service.nextScene(new CurrentUser(1L), 20L, 1L, 2L))
                .isInstanceOfSatisfying(TrainingServiceException.class, exception -> {
                    assertThat(exception.errorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                    assertThat(exception.getMessage()).isEqualTo("Training session belongs to another user.");
                });
    }

    @Test
    void validatesOwnershipWhenReadingDetail() {
        ownershipRepository.save(20L, 1L);
        safetyRepository.score = Optional.of(new SafetyTrainingRepository.SafetyScoreRow(70, 7, 10));
        safetyRepository.feedback = Optional.of(new SafetyFeedbackResponse("Safety summary", "Safety detail"));
        safetyRepository.actionDetails = List.of(new SafetyActionDetailResponse(1L, "Wet floor", "Report it", true));

        var response = service.getSessionDetail(new CurrentUser(1L), 20L);

        assertThat(response.score()).isEqualTo(70);
        assertThat(response.choiceSummary().correctCount()).isEqualTo(7);
        assertThat(response.choiceSummary().totalCount()).isEqualTo(10);
        assertThat(response.actions()).hasSize(1);
        assertThat(response.feedback().summary()).isEqualTo("Safety summary");
    }

    private SafetySceneResponse scene(long sceneId, boolean endScene) {
        return new SafetySceneResponse(
                sceneId,
                "screen",
                "situation",
                "question",
                "/scene.png",
                "Scene alt",
                List.of(new SafetyChoiceResponse(2L, "Report it to the manager.")),
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

        @Override
        public List<SafetyScenarioListItemResponse> findActiveScenarios(SafetyCategory category) {
            return List.of(new SafetyScenarioListItemResponse(1L, SafetyCategory.COMMUTE_SAFETY, "Commute safety", "Safety scenario"));
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
            return List.of(new SafetyChoiceResponse(2L, "Report it to the manager."));
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
