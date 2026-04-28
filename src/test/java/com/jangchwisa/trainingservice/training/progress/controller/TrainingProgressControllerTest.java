package com.jangchwisa.trainingservice.training.progress.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jangchwisa.trainingservice.common.exception.GlobalExceptionHandler;
import com.jangchwisa.trainingservice.common.security.CurrentUserArgumentResolver;
import com.jangchwisa.trainingservice.common.security.TrustedUserHeaderProperties;
import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.progress.service.TrainingProgressService;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TrainingProgressControllerTest {

    FakeTrainingProgressRepository repository = new FakeTrainingProgressRepository();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        TrainingProgressService service = new TrainingProgressService(repository);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TrainingProgressController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }

    @Test
    void returnsSocialProgressByDefault() throws Exception {
        repository.socialProgress = Optional.of(new SocialProgressResponse(
                TrainingType.SOCIAL,
                10L,
                85,
                "상황에 맞게 정중하게 대화했습니다.",
                3,
                null
        ));

        mockMvc.perform(get("/api/trainings/progress")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingType").value("SOCIAL"))
                .andExpect(jsonPath("$.data.recentSessionId").value(10))
                .andExpect(jsonPath("$.data.recentScore").value(85))
                .andExpect(jsonPath("$.data.recentFeedbackSummary").value("상황에 맞게 정중하게 대화했습니다."))
                .andExpect(jsonPath("$.data.completedCount").value(3));
    }

    @Test
    void returnsSafetyProgressForRequestedType() throws Exception {
        repository.safetyProgress = Optional.of(new SafetyProgressResponse(
                TrainingType.SAFETY,
                20L,
                7,
                10,
                2,
                null
        ));

        mockMvc.perform(get("/api/trainings/progress")
                        .param("type", "SAFETY")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trainingType").value("SAFETY"))
                .andExpect(jsonPath("$.data.recentSessionId").value(20))
                .andExpect(jsonPath("$.data.correctCount").value(7))
                .andExpect(jsonPath("$.data.totalCount").value(10))
                .andExpect(jsonPath("$.data.completedCount").value(2));
    }

    @Test
    void returnsValidationErrorForInvalidType() throws Exception {
        mockMvc.perform(get("/api/trainings/progress")
                        .param("type", "VOICE")
                        .header("X-User-Id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Training type is invalid."));
    }

    @Test
    void requiresTrustedUserHeader() throws Exception {
        mockMvc.perform(get("/api/trainings/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    static class FakeTrainingProgressRepository implements TrainingProgressRepository {

        Optional<SocialProgressResponse> socialProgress = Optional.empty();
        Optional<SafetyProgressResponse> safetyProgress = Optional.empty();
        Optional<DocumentProgressResponse> documentProgress = Optional.empty();
        Optional<FocusProgressResponse> focusProgress = Optional.empty();

        @Override
        public Optional<SocialProgressResponse> findSocialProgress(long userId) {
            return socialProgress;
        }

        @Override
        public Optional<SafetyProgressResponse> findSafetyProgress(long userId) {
            return safetyProgress;
        }

        @Override
        public Optional<DocumentProgressResponse> findDocumentProgress(long userId) {
            return documentProgress;
        }

        @Override
        public Optional<FocusProgressResponse> findFocusProgress(long userId) {
            return focusProgress;
        }
    }
}
