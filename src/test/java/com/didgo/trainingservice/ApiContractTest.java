package com.didgo.trainingservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.didgo.trainingservice.common.exception.GlobalExceptionHandler;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.common.security.CurrentUserArgumentResolver;
import com.didgo.trainingservice.common.security.TrustedUserHeaderProperties;
import com.didgo.trainingservice.support.MockMvcTestSupport;
import com.didgo.trainingservice.training.document.controller.DocumentTrainingController;
import com.didgo.trainingservice.training.document.service.DocumentTrainingService;
import com.didgo.trainingservice.training.focus.controller.FocusTrainingController;
import com.didgo.trainingservice.training.focus.dto.CompleteFocusSessionRequest;
import com.didgo.trainingservice.training.focus.dto.CompleteFocusSessionResponse;
import com.didgo.trainingservice.training.focus.service.FocusTrainingService;
import com.didgo.trainingservice.training.progress.controller.TrainingProgressController;
import com.didgo.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.didgo.trainingservice.training.progress.dto.TrainingLevelResponse;
import com.didgo.trainingservice.training.progress.dto.TrainingProgressSummaryResponse;
import com.didgo.trainingservice.training.progress.service.TrainingProgressService;
import com.didgo.trainingservice.training.safety.controller.SafetyTrainingController;
import com.didgo.trainingservice.training.safety.dto.CompleteSafetySessionResponse;
import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
import com.didgo.trainingservice.training.safety.service.SafetyTrainingService;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.social.controller.SocialTrainingController;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionRequest;
import com.didgo.trainingservice.training.social.dto.CompleteSocialSessionResponse;
import com.didgo.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.didgo.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.didgo.trainingservice.training.social.dto.SocialSessionDetailResponse;
import com.didgo.trainingservice.training.social.entity.SocialDialogSpeaker;
import com.didgo.trainingservice.training.social.service.SocialTrainingService;
import com.didgo.trainingservice.training.summary.controller.TrainingSessionListController;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListResponse;
import com.didgo.trainingservice.training.summary.service.TrainingSessionListService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class ApiContractTest {

    @Test
    void progressApiMatchesContractAndUsesTrustedHeaderUserId() throws Exception {
        TrainingProgressService service = mock(TrainingProgressService.class);
        when(service.getProgress(7L, TrainingType.SOCIAL)).thenReturn(new TrainingLevelResponse(
                TrainingType.SOCIAL,
                4,
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 5, 1, 0, 0),
                "Asia/Seoul",
                3,
                3,
                "MONTHLY_COMPLETED_SUMMARIES",
                null,
                Map.of(
                        "averageScore", BigDecimal.valueOf(80.0),
                        "monthlyCompletedCount", 3
                )
        ));

        mockMvc(new TrainingProgressController(service))
                .perform(get("/api/trainings/progress")
                        .param("type", "SOCIAL")
                        .param("userId", "999")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingType").value("SOCIAL"))
                .andExpect(jsonPath("$.data.level").value(4))
                .andExpect(jsonPath("$.data.periodStart").value("2026-04-01T00:00:00"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-05-01T00:00:00"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.data.completedCount").value(3))
                .andExpect(jsonPath("$.data.minRequiredCount").value(3))
                .andExpect(jsonPath("$.data.basis").value("MONTHLY_COMPLETED_SUMMARIES"))
                .andExpect(jsonPath("$.data.metrics.averageScore").value(80.0));

        verify(service).getProgress(7L, TrainingType.SOCIAL);
    }

    @Test
    void progressSummaryApiMatchesContractAndUsesTrustedHeaderUserId() throws Exception {
        TrainingProgressService service = mock(TrainingProgressService.class);
        when(service.getProgressSummary(7L)).thenReturn(new TrainingProgressSummaryResponse(
                LocalDateTime.of(2026, 4, 1, 0, 0),
                LocalDateTime.of(2026, 5, 1, 0, 0),
                "Asia/Seoul",
                List.of(
                        new TrainingLevelResponse(
                                TrainingType.SOCIAL,
                                4,
                                LocalDateTime.of(2026, 4, 1, 0, 0),
                                LocalDateTime.of(2026, 5, 1, 0, 0),
                                "Asia/Seoul",
                                3,
                                3,
                                "MONTHLY_COMPLETED_SUMMARIES",
                                null,
                                Map.of("averageScore", BigDecimal.valueOf(80.0), "monthlyCompletedCount", 3)
                        ),
                        new TrainingLevelResponse(
                                TrainingType.FOCUS,
                                2,
                                LocalDateTime.of(2026, 4, 1, 0, 0),
                                LocalDateTime.of(2026, 5, 1, 0, 0),
                                "Asia/Seoul",
                                1,
                                1,
                                "MONTHLY_COMPLETED_SUMMARIES",
                                null,
                                Map.of("highestPlayedLevel", 2, "monthlyCompletedCount", 1)
                        )
                )
        ));

        mockMvc(new TrainingProgressController(service))
                .perform(get("/api/trainings/progress/summary")
                        .param("userId", "999")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.periodStart").value("2026-04-01T00:00:00"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-05-01T00:00:00"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.data.items[0].trainingType").value("SOCIAL"))
                .andExpect(jsonPath("$.data.items[0].level").value(4))
                .andExpect(jsonPath("$.data.items[1].trainingType").value("FOCUS"))
                .andExpect(jsonPath("$.data.items[1].level").value(2));

        verify(service).getProgressSummary(7L);
    }

    @Test
    void sessionListApiMatchesContractAndUsesTrustedHeaderUserId() throws Exception {
        TrainingSessionListService service = mock(TrainingSessionListService.class);
        when(service.getSessions(7L, TrainingType.SAFETY, 1, 5))
                .thenReturn(new TrainingSessionListResponse(
                        TrainingType.SAFETY,
                        1,
                        5,
                        1,
                        List.of(new TrainingSessionListItemResponse(
                                20L,
                                2L,
                                "Commute safety",
                                SafetyCategory.COMMUTE_SAFETY,
                                70,
                                null,
                                7,
                                10,
                                null,
                                null,
                                null,
                                null,
                                LocalDateTime.of(2026, 4, 27, 10, 20)
                        ))
                ));

        mockMvc(new TrainingSessionListController(service))
                .perform(get("/api/trainings/sessions")
                        .param("type", "SAFETY")
                        .param("page", "1")
                        .param("size", "5")
                        .param("userId", "999")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingType").value("SAFETY"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.sessions[0].sessionId").value(20))
                .andExpect(jsonPath("$.data.sessions[0].scenarioId").value(2))
                .andExpect(jsonPath("$.data.sessions[0].scenarioTitle").value("Commute safety"))
                .andExpect(jsonPath("$.data.sessions[0].category").value("COMMUTE_SAFETY"))
                .andExpect(jsonPath("$.data.sessions[0].score").value(70))
                .andExpect(jsonPath("$.data.sessions[0].correctCount").value(7))
                .andExpect(jsonPath("$.data.sessions[0].totalCount").value(10))
                .andExpect(jsonPath("$.data.sessions[0].completedAt").value("2026-04-27T10:20:00"));

        verify(service).getSessions(7L, TrainingType.SAFETY, 1, 5);
    }

    @Test
    void detailApiMatchesContractAndUsesTrustedHeaderUserId() throws Exception {
        SocialTrainingService service = mock(SocialTrainingService.class);
        when(service.getSessionDetail(any(CurrentUser.class), eq(10L))).thenReturn(new SocialSessionDetailResponse(
                10L,
                85,
                "AI_EVALUATION",
                new SocialFeedbackResponse("Good response", "Detailed feedback"),
                List.of(
                        new SocialDialogLogResponse(1, SocialDialogSpeaker.USER, "Can you help me?"),
                        new SocialDialogLogResponse(1, SocialDialogSpeaker.AI, "Sure.")
                )
        ));

        mockMvc(new SocialTrainingController(service))
                .perform(get("/api/trainings/social/sessions/10/detail")
                        .param("userId", "999")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(10))
                .andExpect(jsonPath("$.data.score").value(85))
                .andExpect(jsonPath("$.data.scoreType").value("AI_EVALUATION"))
                .andExpect(jsonPath("$.data.feedback.summary").value("Good response"))
                .andExpect(jsonPath("$.data.feedback.detailText").value("Detailed feedback"))
                .andExpect(jsonPath("$.data.dialogLogs[0].turnNo").value(1))
                .andExpect(jsonPath("$.data.dialogLogs[0].speaker").value("USER"))
                .andExpect(jsonPath("$.data.dialogLogs[0].content").value("Can you help me?"));

        ArgumentCaptor<CurrentUser> currentUser = ArgumentCaptor.forClass(CurrentUser.class);
        verify(service).getSessionDetail(currentUser.capture(), eq(10L));
        assertThat(currentUser.getValue().userId()).isEqualTo(7L);
    }

    @Test
    void socialCompletionApiMatchesContractAndDoesNotTrustBodyUserId() throws Exception {
        SocialTrainingService service = mock(SocialTrainingService.class);
        when(service.completeSession(any(CurrentUser.class), eq(10L), any(CompleteSocialSessionRequest.class)))
                .thenReturn(new CompleteSocialSessionResponse(10L, 85, "Good response", true));

        mockMvc(new SocialTrainingController(service))
                .perform(post("/api/trainings/social/sessions/10/complete")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 999,
                                  "dialogLogs": [
                                    {"turnNo": 1, "speaker": "USER", "content": "Can you help me?"},
                                    {"turnNo": 1, "speaker": "AI", "content": "Sure."}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(10))
                .andExpect(jsonPath("$.data.score").value(85))
                .andExpect(jsonPath("$.data.feedbackSummary").value("Good response"))
                .andExpect(jsonPath("$.data.completed").value(true));

        ArgumentCaptor<CurrentUser> currentUser = ArgumentCaptor.forClass(CurrentUser.class);
        ArgumentCaptor<CompleteSocialSessionRequest> request = ArgumentCaptor.forClass(CompleteSocialSessionRequest.class);
        verify(service).completeSession(currentUser.capture(), eq(10L), request.capture());
        assertThat(currentUser.getValue().userId()).isEqualTo(7L);
        assertThat(request.getValue().dialogLogs()).hasSize(2);
    }

    @Test
    void safetyAndFocusCompletionApisMatchContracts() throws Exception {
        SafetyTrainingService safetyService = mock(SafetyTrainingService.class);
        FocusTrainingService focusService = mock(FocusTrainingService.class);
        when(safetyService.completeSession(any(CurrentUser.class), eq(20L)))
                .thenReturn(new CompleteSafetySessionResponse(20L, 70, 7, 10, true));
        when(focusService.completeSession(any(CurrentUser.class), eq(30L), any(CompleteFocusSessionRequest.class)))
                .thenReturn(new CompleteFocusSessionResponse(
                        30L,
                        92,
                        BigDecimal.valueOf(92.5),
                        3,
                        820,
                        true,
                        3,
                        3
                ));

        mockMvc(new SafetyTrainingController(safetyService))
                .perform(post("/api/trainings/safety/sessions/20/complete")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(20))
                .andExpect(jsonPath("$.data.score").value(70))
                .andExpect(jsonPath("$.data.correctCount").value(7))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.completed").value(true));

        mockMvc(new FocusTrainingController(focusService))
                .perform(post("/api/trainings/focus/sessions/30/complete")
                        .header("X-User-Id", "7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reactions":[{"commandId":1,"userInput":"BLUE_UP","reactionMs":820}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(30))
                .andExpect(jsonPath("$.data.score").value(92))
                .andExpect(jsonPath("$.data.accuracyRate").value(92.5))
                .andExpect(jsonPath("$.data.wrongCount").value(3))
                .andExpect(jsonPath("$.data.averageReactionMs").value(820))
                .andExpect(jsonPath("$.data.unlockedNextLevel").value(true))
                .andExpect(jsonPath("$.data.currentLevel").value(3))
                .andExpect(jsonPath("$.data.highestUnlockedLevel").value(3));
    }

    @Test
    void documentProgressApiMatchesContractAndUsesTrustedHeaderUserId() throws Exception {
        DocumentTrainingService service = mock(DocumentTrainingService.class);
        when(service.getProgress(7L)).thenReturn(new DocumentProgressResponse(
                TrainingType.DOCUMENT,
                50L,
                4,
                5,
                80,
                2,
                3,
                2,
                BigDecimal.valueOf(80.0),
                4,
                LocalDateTime.of(2026, 4, 27, 10, 40)
        ));

        mockMvc(new DocumentTrainingController(service))
                .perform(get("/api/trainings/document/progress")
                        .param("userId", "999")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingType").value("DOCUMENT"))
                .andExpect(jsonPath("$.data.currentLevel").value(2))
                .andExpect(jsonPath("$.data.highestUnlockedLevel").value(3))
                .andExpect(jsonPath("$.data.lastPlayedLevel").value(2))
                .andExpect(jsonPath("$.data.lastAccuracyRate").value(80.0));

        verify(service).getProgress(7L);
    }

    private MockMvc mockMvc(Object controller) {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        return MockMvcTestSupport
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }
}
