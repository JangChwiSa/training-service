package com.jangchwisa.trainingservice.training.social.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jangchwisa.trainingservice.common.exception.GlobalExceptionHandler;
import com.jangchwisa.trainingservice.common.security.CurrentUserArgumentResolver;
import com.jangchwisa.trainingservice.common.security.TrustedUserHeaderProperties;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionOwnershipRepository;
import com.jangchwisa.trainingservice.training.session.repository.TrainingSessionRepository;
import com.jangchwisa.trainingservice.training.session.service.SessionOwnershipValidator;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import com.jangchwisa.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.social.entity.SocialDialogSpeaker;
import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import com.jangchwisa.trainingservice.training.social.repository.SocialTrainingRepository;
import com.jangchwisa.trainingservice.training.social.service.SocialTrainingService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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

class SocialTrainingControllerTest {

    FakeOwnershipRepository ownershipRepository = new FakeOwnershipRepository();
    FakeSocialTrainingRepository socialRepository = new FakeSocialTrainingRepository();
    FakeTrainingSessionRepository sessionRepository = new FakeTrainingSessionRepository(ownershipRepository);
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        TrainingSessionService trainingSessionService = new TrainingSessionService(
                sessionRepository,
                Clock.fixed(Instant.parse("2026-04-28T01:00:00Z"), ZoneId.of("Asia/Seoul"))
        );
        SocialTrainingService socialTrainingService = new SocialTrainingService(
                socialRepository,
                trainingSessionService,
                new SessionOwnershipValidator(ownershipRepository)
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SocialTrainingController(socialTrainingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }

    @Test
    void selectsJobTypeWithoutPersistingIt() throws Exception {
        mockMvc.perform(post("/api/trainings/social/job-type")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobType\":\"OFFICE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobType").value("OFFICE"))
                .andExpect(jsonPath("$.data.nextPage").value("SCENARIO_SELECTION"));
    }

    @Test
    void returnsScenarioListByJobType() throws Exception {
        mockMvc.perform(get("/api/trainings/social/scenarios")
                        .param("jobType", "OFFICE")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].scenarioId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("동료에게 도움 요청하기"))
                .andExpect(jsonPath("$.data[0].difficulty").value(1));
    }

    @Test
    void returnsScenarioDetail() throws Exception {
        mockMvc.perform(get("/api/trainings/social/scenarios/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scenarioId").value(1))
                .andExpect(jsonPath("$.data.jobType").value("OFFICE"))
                .andExpect(jsonPath("$.data.backgroundText").value("배경"))
                .andExpect(jsonPath("$.data.situationText").value("상황"));
    }

    @Test
    void startsSocialSession() throws Exception {
        socialRepository.activeScenarios.put(1L, SocialJobType.OFFICE);

        mockMvc.perform(post("/api/trainings/social/sessions")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobType\":\"OFFICE\",\"scenarioId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.scenarioId").value(1))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        org.assertj.core.api.Assertions.assertThat(sessionRepository.sessions.get(1L).subType()).isEqualTo("OFFICE");
    }

    @Test
    void returnsSessionDetailAfterOwnershipValidation() throws Exception {
        ownershipRepository.save(10L, 1L);
        socialRepository.score = Optional.of(new SocialTrainingRepository.SocialScoreRow(85, "AI_EVALUATION"));
        socialRepository.feedback = Optional.of(new SocialFeedbackResponse("상황에 맞게 대화했습니다.", "상세 피드백"));
        socialRepository.dialogLogs = List.of(
                new SocialDialogLogResponse(1, SocialDialogSpeaker.USER, "도와주실 수 있나요?"),
                new SocialDialogLogResponse(1, SocialDialogSpeaker.AI, "네, 어떤 부분이 어려우신가요?")
        );

        mockMvc.perform(get("/api/trainings/social/sessions/10/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(10))
                .andExpect(jsonPath("$.data.score").value(85))
                .andExpect(jsonPath("$.data.scoreType").value("AI_EVALUATION"))
                .andExpect(jsonPath("$.data.feedback.summary").value("상황에 맞게 대화했습니다."))
                .andExpect(jsonPath("$.data.dialogLogs[0].speaker").value("USER"))
                .andExpect(jsonPath("$.data.dialogLogs[1].speaker").value("AI"));
    }

    @Test
    void rejectsAnotherUsersSessionDetail() throws Exception {
        ownershipRepository.save(10L, 2L);

        mockMvc.perform(get("/api/trainings/social/sessions/10/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.error.message").value("Training session belongs to another user."));
    }

    @Test
    void returnsNotFoundWhenSessionDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/trainings/social/sessions/999/detail")
                        .header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Training session was not found."));
    }

    @Test
    void rejectsInvalidJobType() throws Exception {
        mockMvc.perform(get("/api/trainings/social/scenarios")
                        .param("jobType", "UNKNOWN")
                        .header("X-User-Id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Social job type is invalid."));
    }

    static class FakeSocialTrainingRepository implements SocialTrainingRepository {

        Map<Long, SocialJobType> activeScenarios = new HashMap<>();
        Optional<SocialScoreRow> score = Optional.empty();
        Optional<SocialFeedbackResponse> feedback = Optional.empty();
        List<SocialDialogLogResponse> dialogLogs = List.of();

        @Override
        public List<SocialScenarioListItemResponse> findActiveScenariosByJobType(SocialJobType jobType) {
            return List.of(new SocialScenarioListItemResponse(1L, "동료에게 도움 요청하기", 1));
        }

        @Override
        public Optional<SocialScenarioDetailResponse> findActiveScenarioDetail(long scenarioId) {
            return Optional.of(new SocialScenarioDetailResponse(scenarioId, SocialJobType.OFFICE, "제목", "배경", "상황", "동료", 1));
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
