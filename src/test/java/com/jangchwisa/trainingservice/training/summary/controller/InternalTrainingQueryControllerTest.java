package com.jangchwisa.trainingservice.training.summary.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jangchwisa.trainingservice.common.exception.GlobalExceptionHandler;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.jangchwisa.trainingservice.training.summary.dto.LatestTrainingResultResponse;
import com.jangchwisa.trainingservice.training.summary.repository.InternalTrainingQueryRepository;
import com.jangchwisa.trainingservice.training.summary.service.InternalTrainingQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InternalTrainingQueryControllerTest {

    FakeInternalTrainingQueryRepository repository = new FakeInternalTrainingQueryRepository();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalTrainingQueryService service = new InternalTrainingQueryService(repository);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new InternalTrainingQueryController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsTrainingSummaryByPathUserId() throws Exception {
        repository.summary = new InternalTrainingSummaryResponse(85, 7, 10, 8, 10, 3);

        mockMvc.perform(get("/internal/trainings/users/1/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.socialRecentScore").value(85))
                .andExpect(jsonPath("$.data.safetyCorrectCount").value(7))
                .andExpect(jsonPath("$.data.safetyTotalCount").value(10))
                .andExpect(jsonPath("$.data.documentCorrectCount").value(8))
                .andExpect(jsonPath("$.data.documentTotalCount").value(10))
                .andExpect(jsonPath("$.data.focusCurrentLevel").value(3));
    }

    @Test
    void returnsLatestResultsByPathUserId() throws Exception {
        repository.latestResults = List.of(new LatestTrainingResultResponse(
                10L,
                TrainingType.SOCIAL,
                85,
                "AI_EVALUATION",
                LocalDateTime.of(2026, 4, 27, 10, 30)
        ));

        mockMvc.perform(get("/internal/trainings/users/1/latest-results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.results[0].sessionId").value(10))
                .andExpect(jsonPath("$.data.results[0].trainingType").value("SOCIAL"))
                .andExpect(jsonPath("$.data.results[0].score").value(85))
                .andExpect(jsonPath("$.data.results[0].scoreType").value("AI_EVALUATION"))
                .andExpect(jsonPath("$.data.results[0].completedAt").value("2026-04-27T10:30:00"));
    }

    @Test
    void rejectsInvalidPathUserId() throws Exception {
        mockMvc.perform(get("/internal/trainings/users/0/summary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("User id must be positive."));
    }

    static class FakeInternalTrainingQueryRepository implements InternalTrainingQueryRepository {

        InternalTrainingSummaryResponse summary = new InternalTrainingSummaryResponse(null, 0, 0, 0, 0, 1);
        List<LatestTrainingResultResponse> latestResults = List.of();

        @Override
        public InternalTrainingSummaryResponse findTrainingSummary(long userId) {
            return summary;
        }

        @Override
        public List<LatestTrainingResultResponse> findLatestTrainingResults(long userId) {
            return latestResults;
        }
    }
}
