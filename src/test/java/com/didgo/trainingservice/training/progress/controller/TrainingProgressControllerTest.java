package com.didgo.trainingservice.training.progress.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.didgo.trainingservice.common.exception.GlobalExceptionHandler;
import com.didgo.trainingservice.common.security.CurrentUserArgumentResolver;
import com.didgo.trainingservice.common.security.TrustedUserHeaderProperties;
import com.didgo.trainingservice.support.MockMvcTestSupport;
import com.didgo.trainingservice.training.progress.entity.MonthlyTrainingSummaryEntry;
import com.didgo.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.didgo.trainingservice.training.progress.service.TrainingProgressService;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

class TrainingProgressControllerTest {

    FakeTrainingProgressRepository repository = new FakeTrainingProgressRepository();
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrustedUserHeaderProperties properties = new TrustedUserHeaderProperties();
        Clock clock = Clock.fixed(Instant.parse("2026-04-29T03:00:00Z"), ZoneId.of("UTC"));
        TrainingProgressService service = new TrainingProgressService(repository, clock);
        mockMvc = MockMvcTestSupport
                .standaloneSetup(new TrainingProgressController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver(properties))
                .build();
    }

    @Test
    void returnsSocialLevelByDefault() throws Exception {
        repository.entries = List.of(
                new MonthlyTrainingSummaryEntry(70, null, null, null),
                new MonthlyTrainingSummaryEntry(80, null, null, null),
                new MonthlyTrainingSummaryEntry(90, null, null, null)
        );

        mockMvc.perform(get("/api/trainings/progress")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trainingType").value("SOCIAL"))
                .andExpect(jsonPath("$.data.level").value(4))
                .andExpect(jsonPath("$.data.periodStart").value("2026-04-01T00:00:00"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-05-01T00:00:00"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.data.completedCount").value(3))
                .andExpect(jsonPath("$.data.minRequiredCount").value(3))
                .andExpect(jsonPath("$.data.reason").doesNotExist())
                .andExpect(jsonPath("$.data.metrics.averageScore").value(80.0))
                .andExpect(jsonPath("$.data.metrics.monthlyCompletedCount").value(3));
    }

    @Test
    void returnsSafetyLevelForRequestedType() throws Exception {
        repository.entries = List.of(
                new MonthlyTrainingSummaryEntry(95, "COMMUTE_SAFETY", null, null),
                new MonthlyTrainingSummaryEntry(95, "INFECTIOUS_DISEASE", null, null),
                new MonthlyTrainingSummaryEntry(95, "COMMUTE_SAFETY", null, null)
        );

        mockMvc.perform(get("/api/trainings/progress")
                        .param("type", "SAFETY")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trainingType").value("SAFETY"))
                .andExpect(jsonPath("$.data.level").value(4))
                .andExpect(jsonPath("$.data.metrics.coveredCategoryCount").value(2))
                .andExpect(jsonPath("$.data.metrics.coveredCategories[0]").value("COMMUTE_SAFETY"))
                .andExpect(jsonPath("$.data.metrics.coveredCategories[1]").value("INFECTIOUS_DISEASE"));
    }

    @Test
    void returnsHomeProgressSummaryForAllTrainingTypes() throws Exception {
        repository.entriesByType.put(TrainingType.SOCIAL, List.of(
                new MonthlyTrainingSummaryEntry(70, null, null, null),
                new MonthlyTrainingSummaryEntry(80, null, null, null),
                new MonthlyTrainingSummaryEntry(90, null, null, null)
        ));
        repository.entriesByType.put(TrainingType.SAFETY, List.of(
                new MonthlyTrainingSummaryEntry(95, "COMMUTE_SAFETY", null, null),
                new MonthlyTrainingSummaryEntry(95, "INFECTIOUS_DISEASE", null, null),
                new MonthlyTrainingSummaryEntry(95, "COMMUTE_SAFETY", null, null)
        ));
        repository.entriesByType.put(TrainingType.DOCUMENT, List.of(
                new MonthlyTrainingSummaryEntry(70, null, null, "LEVEL_3")
        ));
        repository.entriesByType.put(TrainingType.FOCUS, List.of(
                new MonthlyTrainingSummaryEntry(70, null, 2, null)
        ));

        mockMvc.perform(get("/api/trainings/progress/summary")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.periodStart").value("2026-04-01T00:00:00"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-05-01T00:00:00"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Seoul"))
                .andExpect(jsonPath("$.data.items[0].trainingType").value("SOCIAL"))
                .andExpect(jsonPath("$.data.items[0].level").value(4))
                .andExpect(jsonPath("$.data.items[1].trainingType").value("SAFETY"))
                .andExpect(jsonPath("$.data.items[1].level").value(4))
                .andExpect(jsonPath("$.data.items[2].trainingType").value("DOCUMENT"))
                .andExpect(jsonPath("$.data.items[2].level").value(3))
                .andExpect(jsonPath("$.data.items[3].trainingType").value("FOCUS"))
                .andExpect(jsonPath("$.data.items[3].level").value(2));
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

        List<MonthlyTrainingSummaryEntry> entries = List.of();
        Map<TrainingType, List<MonthlyTrainingSummaryEntry>> entriesByType = new EnumMap<>(TrainingType.class);

        @Override
        public List<MonthlyTrainingSummaryEntry> findMonthlyCompletedSummaries(
                long userId,
                TrainingType trainingType,
                LocalDateTime periodStart,
                LocalDateTime periodEnd
        ) {
            return entriesByType.getOrDefault(trainingType, entries);
        }
    }
}
