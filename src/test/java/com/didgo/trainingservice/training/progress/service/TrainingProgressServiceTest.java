package com.didgo.trainingservice.training.progress.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.training.progress.dto.TrainingLevelResponse;
import com.didgo.trainingservice.training.progress.dto.TrainingProgressSummaryResponse;
import com.didgo.trainingservice.training.progress.entity.MonthlyTrainingSummaryEntry;
import com.didgo.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrainingProgressServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-29T03:00:00Z"),
            ZoneId.of("UTC")
    );

    FakeTrainingProgressRepository repository = new FakeTrainingProgressRepository();
    TrainingProgressService service = new TrainingProgressService(repository, FIXED_CLOCK);

    @Test
    void returnsSocialLevelFromMonthlyAverageScoreWhenAtLeastThreeCompletionsExist() {
        repository.entries = List.of(
                entry(70, null, null, null),
                entry(80, null, null, null),
                entry(90, null, null, null)
        );

        TrainingLevelResponse response = (TrainingLevelResponse) service.getProgress(1L, TrainingType.SOCIAL);

        assertThat(response.level()).isEqualTo(4);
        assertThat(response.completedCount()).isEqualTo(3);
        assertThat(response.minRequiredCount()).isEqualTo(3);
        assertThat(response.reason()).isNull();
        assertThat(response.metrics()).containsEntry("averageScore", BigDecimal.valueOf(80.0));
        assertThat(response.metrics()).containsEntry("monthlyCompletedCount", 3);
    }

    @Test
    void returnsNullSocialLevelWhenMonthlyCompletionsAreInsufficient() {
        repository.entries = List.of(
                entry(95, null, null, null),
                entry(95, null, null, null)
        );

        TrainingLevelResponse response = (TrainingLevelResponse) service.getProgress(1L, TrainingType.SOCIAL);

        assertThat(response.level()).isNull();
        assertThat(response.reason()).isEqualTo("INSUFFICIENT_COMPLETIONS");
        assertThat(response.completedCount()).isEqualTo(2);
    }

    @Test
    void returnsHighestDocumentLevelFromMonthlyCompletedSessionSubType() {
        repository.entries = List.of(
                entry(70, null, null, "LEVEL_2"),
                entry(90, null, null, "LEVEL_5"),
                entry(80, null, null, "LEVEL_3")
        );

        TrainingLevelResponse response = (TrainingLevelResponse) service.getProgress(1L, TrainingType.DOCUMENT);

        assertThat(response.level()).isEqualTo(5);
        assertThat(response.metrics()).containsEntry("highestCompletedLevel", 5);
    }

    @Test
    void returnsHighestFocusPlayedLevelFromMonthlySummaries() {
        repository.entries = List.of(
                entry(70, null, 2, null),
                entry(90, null, 4, null),
                entry(80, null, 3, null)
        );

        TrainingLevelResponse response = (TrainingLevelResponse) service.getProgress(1L, TrainingType.FOCUS);

        assertThat(response.level()).isEqualTo(4);
        assertThat(response.metrics()).containsEntry("highestPlayedLevel", 4);
    }

    @Test
    void returnsSafetyLevelUsingAverageScoreCappedByCoveredCategoryCount() {
        repository.entries = List.of(
                entry(95, "COMMUTE_SAFETY", null, null),
                entry(95, "INFECTIOUS_DISEASE", null, null),
                entry(95, "COMMUTE_SAFETY", null, null)
        );

        TrainingLevelResponse response = (TrainingLevelResponse) service.getProgress(1L, TrainingType.SAFETY);

        assertThat(response.level()).isEqualTo(4);
        assertThat(response.metrics()).containsEntry("averageScore", BigDecimal.valueOf(95.0));
        assertThat(response.metrics()).containsEntry("coveredCategoryCount", 2);
        assertThat(response.metrics()).containsEntry("coveredCategories", List.of("COMMUTE_SAFETY", "INFECTIOUS_DISEASE"));
    }

    @Test
    void returnsNullLevelWhenMonthlyCompletionDoesNotExist() {
        TrainingLevelResponse response = (TrainingLevelResponse) service.getProgress(1L, TrainingType.DOCUMENT);

        assertThat(response.level()).isNull();
        assertThat(response.reason()).isEqualTo("NO_MONTHLY_COMPLETION");
        assertThat(response.periodStart()).isEqualTo(LocalDateTime.of(2026, 4, 1, 0, 0));
        assertThat(response.periodEnd()).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
        assertThat(response.timezone()).isEqualTo("Asia/Seoul");
    }

    @Test
    void queriesCurrentCalendarMonthInAsiaSeoul() {
        service.getProgress(12L, TrainingType.FOCUS);

        assertThat(repository.userId).isEqualTo(12L);
        assertThat(repository.trainingType).isEqualTo(TrainingType.FOCUS);
        assertThat(repository.periodStart).isEqualTo(LocalDateTime.of(2026, 4, 1, 0, 0));
        assertThat(repository.periodEnd).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
    }

    @Test
    void returnsHomeSummaryForAllTrainingTypes() {
        repository.entriesByType.put(TrainingType.SOCIAL, List.of(
                entry(70, null, null, null),
                entry(80, null, null, null),
                entry(90, null, null, null)
        ));
        repository.entriesByType.put(TrainingType.SAFETY, List.of(
                entry(95, "COMMUTE_SAFETY", null, null),
                entry(95, "INFECTIOUS_DISEASE", null, null),
                entry(95, "COMMUTE_SAFETY", null, null)
        ));
        repository.entriesByType.put(TrainingType.DOCUMENT, List.of(entry(70, null, null, "LEVEL_3")));
        repository.entriesByType.put(TrainingType.FOCUS, List.of(entry(70, null, 2, null)));

        TrainingProgressSummaryResponse response = service.getProgressSummary(12L);

        assertThat(response.periodStart()).isEqualTo(LocalDateTime.of(2026, 4, 1, 0, 0));
        assertThat(response.periodEnd()).isEqualTo(LocalDateTime.of(2026, 5, 1, 0, 0));
        assertThat(response.timezone()).isEqualTo("Asia/Seoul");
        assertThat(response.items()).extracting(TrainingLevelResponse::trainingType)
                .containsExactly(TrainingType.SOCIAL, TrainingType.SAFETY, TrainingType.DOCUMENT, TrainingType.FOCUS);
        assertThat(response.items()).extracting(TrainingLevelResponse::level)
                .containsExactly(4, 4, 3, 2);
        assertThat(repository.queriedTypes)
                .containsExactly(TrainingType.SOCIAL, TrainingType.SAFETY, TrainingType.DOCUMENT, TrainingType.FOCUS);
    }

    private static MonthlyTrainingSummaryEntry entry(Integer score, String category, Integer playedLevel, String subType) {
        return new MonthlyTrainingSummaryEntry(score, category, playedLevel, subType);
    }

    static class FakeTrainingProgressRepository implements TrainingProgressRepository {

        List<MonthlyTrainingSummaryEntry> entries = new ArrayList<>();
        Map<TrainingType, List<MonthlyTrainingSummaryEntry>> entriesByType = new EnumMap<>(TrainingType.class);
        List<TrainingType> queriedTypes = new ArrayList<>();
        long userId;
        TrainingType trainingType;
        LocalDateTime periodStart;
        LocalDateTime periodEnd;

        @Override
        public List<MonthlyTrainingSummaryEntry> findMonthlyCompletedSummaries(
                long userId,
                TrainingType trainingType,
                LocalDateTime periodStart,
                LocalDateTime periodEnd
        ) {
            this.userId = userId;
            this.trainingType = trainingType;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.queriedTypes.add(trainingType);
            return entriesByType.getOrDefault(trainingType, entries);
        }
    }
}
