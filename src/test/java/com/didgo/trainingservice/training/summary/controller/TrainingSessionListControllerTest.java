package com.didgo.trainingservice.training.summary.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.didgo.trainingservice.common.exception.GlobalExceptionHandler;
import com.didgo.trainingservice.common.security.CurrentUserArgumentResolver;
import com.didgo.trainingservice.common.security.TrustedUserHeaderProperties;
import com.didgo.trainingservice.support.MockMvcTestSupport;
import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.didgo.trainingservice.training.summary.repository.TrainingSessionSummaryRepository;
import com.didgo.trainingservice.training.summary.service.TrainingSessionListService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

class TrainingSessionListControllerTest {

    FakeTrainingSessionSummaryRepository repository = new FakeTrainingSessionSummaryRepository();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        TrainingSessionListService service = new TrainingSessionListService(repository);
        mockMvc = MockMvcTestSupport
                .standaloneSetup(new TrainingSessionListController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }

    @Test
    void returnsSocialSessionListWithDefaultPaging() throws Exception {
        repository.totalElements = 1L;
        repository.sessions = List.of(new TrainingSessionListItemResponse(
                10L,
                1L,
                "??뉗┷?癒?쓺 ?袁? ?遺욧퍕??띾┛",
                null,
                85,
                "?怨뱀넺??筌띿쉳苡??類ㅼ㉦??띿쓺 ???酉六??щ빍??",
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 4, 27, 10, 0)
        ));

        mockMvc.perform(get("/api/trainings/sessions")
                        .param("type", "SOCIAL")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingType").value("SOCIAL"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.sessions[0].sessionId").value(10))
                .andExpect(jsonPath("$.data.sessions[0].scenarioId").value(1))
                .andExpect(jsonPath("$.data.sessions[0].scenarioTitle").value("??뉗┷?癒?쓺 ?袁? ?遺욧퍕??띾┛"))
                .andExpect(jsonPath("$.data.sessions[0].score").value(85))
                .andExpect(jsonPath("$.data.sessions[0].feedbackSummary").value("?怨뱀넺??筌띿쉳苡??類ㅼ㉦??띿쓺 ???酉六??щ빍??"));
    }

    @Test
    void returnsSafetySessionList() throws Exception {
        repository.totalElements = 1L;
        repository.sessions = List.of(new TrainingSessionListItemResponse(
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
        ));

        mockMvc.perform(get("/api/trainings/sessions")
                        .param("type", "SAFETY")
                        .param("page", "1")
                        .param("size", "5")
                        .header("X-User-Id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trainingType").value("SAFETY"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.sessions[0].category").value("COMMUTE_SAFETY"))
                .andExpect(jsonPath("$.data.sessions[0].correctCount").value(7))
                .andExpect(jsonPath("$.data.sessions[0].totalCount").value(10));

        org.assertj.core.api.Assertions.assertThat(repository.userId).isEqualTo(7L);
        org.assertj.core.api.Assertions.assertThat(repository.trainingType).isEqualTo(TrainingType.SAFETY);
        org.assertj.core.api.Assertions.assertThat(repository.page).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(repository.size).isEqualTo(5);
    }

    @Test
    void returnsFocusSessionList() throws Exception {
        repository.totalElements = 1L;
        repository.sessions = List.of(new TrainingSessionListItemResponse(
                40L,
                null,
                null,
                null,
                92,
                null,
                null,
                null,
                2,
                BigDecimal.valueOf(92.5),
                3,
                820,
                LocalDateTime.of(2026, 4, 27, 11, 0)
        ));

        mockMvc.perform(get("/api/trainings/sessions")
                        .param("type", "FOCUS")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessions[0].sessionId").value(40))
                .andExpect(jsonPath("$.data.sessions[0].playedLevel").value(2))
                .andExpect(jsonPath("$.data.sessions[0].score").value(92))
                .andExpect(jsonPath("$.data.sessions[0].accuracyRate").value(92.5))
                .andExpect(jsonPath("$.data.sessions[0].wrongCount").value(3))
                .andExpect(jsonPath("$.data.sessions[0].averageReactionMs").value(820));
    }

    @Test
    void returnsDocumentSessionListWithPlayedLevel() throws Exception {
        repository.totalElements = 1L;
        repository.sessions = List.of(new TrainingSessionListItemResponse(
                30L,
                null,
                null,
                null,
                80,
                null,
                4,
                5,
                3,
                null,
                null,
                null,
                LocalDateTime.of(2026, 4, 27, 10, 40)
        ));

        mockMvc.perform(get("/api/trainings/sessions")
                        .param("type", "DOCUMENT")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessions[0].sessionId").value(30))
                .andExpect(jsonPath("$.data.sessions[0].score").value(80))
                .andExpect(jsonPath("$.data.sessions[0].correctCount").value(4))
                .andExpect(jsonPath("$.data.sessions[0].totalCount").value(5))
                .andExpect(jsonPath("$.data.sessions[0].playedLevel").value(3))
                .andExpect(jsonPath("$.data.sessions[0].completedAt").value("2026-04-27T10:40:00"));
    }

    @Test
    void requiresTrainingType() throws Exception {
        mockMvc.perform(get("/api/trainings/sessions")
                        .header("X-User-Id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsInvalidPaging() throws Exception {
        mockMvc.perform(get("/api/trainings/sessions")
                        .param("type", "DOCUMENT")
                        .param("page", "-1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Page must not be negative."));
    }

    static class FakeTrainingSessionSummaryRepository implements TrainingSessionSummaryRepository {

        long totalElements;
        List<TrainingSessionListItemResponse> sessions = List.of();
        long userId;
        TrainingType trainingType;
        int page;
        int size;

        @Override
        public long countByUserIdAndTrainingType(long userId, TrainingType trainingType) {
            this.userId = userId;
            this.trainingType = trainingType;
            return totalElements;
        }

        @Override
        public List<TrainingSessionListItemResponse> findByUserIdAndTrainingType(
                long userId,
                TrainingType trainingType,
                int page,
                int size
        ) {
            this.userId = userId;
            this.trainingType = trainingType;
            this.page = page;
            this.size = size;
            return sessions;
        }
    }
}
