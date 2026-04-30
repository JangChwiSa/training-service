package com.jangchwisa.trainingservice.training.progress.service;

import com.jangchwisa.trainingservice.training.progress.dto.TrainingLevelResponse;
import com.jangchwisa.trainingservice.training.progress.dto.TrainingProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.TrainingProgressSummaryResponse;
import com.jangchwisa.trainingservice.training.progress.entity.MonthlyTrainingSummaryEntry;
import com.jangchwisa.trainingservice.training.progress.repository.TrainingProgressRepository;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrainingProgressService {

    private static final ZoneId MONTHLY_ZONE = ZoneId.of("Asia/Seoul");
    private static final String TIMEZONE = MONTHLY_ZONE.getId();
    private static final String BASIS = "MONTHLY_COMPLETED_SUMMARIES";
    private static final String NO_MONTHLY_COMPLETION = "NO_MONTHLY_COMPLETION";
    private static final String INSUFFICIENT_COMPLETIONS = "INSUFFICIENT_COMPLETIONS";

    private final TrainingProgressRepository trainingProgressRepository;
    private final Clock clock;

    @Autowired
    public TrainingProgressService(TrainingProgressRepository trainingProgressRepository, Clock clock) {
        this.trainingProgressRepository = trainingProgressRepository;
        this.clock = clock;
    }

    public TrainingProgressService(TrainingProgressRepository trainingProgressRepository) {
        this(trainingProgressRepository, Clock.system(MONTHLY_ZONE));
    }

    @Transactional(readOnly = true)
    public TrainingProgressResponse getProgress(long userId, TrainingType trainingType) {
        MonthlyPeriod period = monthlyPeriod();
        return getProgress(userId, trainingType, period);
    }

    @Transactional(readOnly = true)
    public TrainingProgressSummaryResponse getProgressSummary(long userId) {
        MonthlyPeriod period = monthlyPeriod();
        List<TrainingLevelResponse> items = List.of(
                getProgress(userId, TrainingType.SOCIAL, period),
                getProgress(userId, TrainingType.SAFETY, period),
                getProgress(userId, TrainingType.DOCUMENT, period),
                getProgress(userId, TrainingType.FOCUS, period)
        );
        return new TrainingProgressSummaryResponse(period.start(), period.end(), TIMEZONE, items);
    }

    private TrainingLevelResponse getProgress(long userId, TrainingType trainingType, MonthlyPeriod period) {
        List<MonthlyTrainingSummaryEntry> entries = trainingProgressRepository.findMonthlyCompletedSummaries(
                userId,
                trainingType,
                period.start(),
                period.end()
        );
        return switch (trainingType) {
            case SOCIAL -> socialLevel(trainingType, period.start(), period.end(), entries);
            case SAFETY -> safetyLevel(trainingType, period.start(), period.end(), entries);
            case DOCUMENT -> documentLevel(trainingType, period.start(), period.end(), entries);
            case FOCUS -> focusLevel(trainingType, period.start(), period.end(), entries);
        };
    }

    private MonthlyPeriod monthlyPeriod() {
        LocalDate firstDay = LocalDate.now(clock.withZone(MONTHLY_ZONE)).withDayOfMonth(1);
        LocalDateTime periodStart = firstDay.atStartOfDay();
        LocalDateTime periodEnd = firstDay.plusMonths(1).atStartOfDay();
        return new MonthlyPeriod(periodStart, periodEnd);
    }

    private static TrainingLevelResponse socialLevel(
            TrainingType trainingType,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<MonthlyTrainingSummaryEntry> entries
    ) {
        int completedCount = entries.size();
        BigDecimal averageScore = averageScore(entries);
        Map<String, Object> metrics = orderedMetrics();
        metrics.put("averageScore", averageScore);
        metrics.put("monthlyCompletedCount", completedCount);

        if (completedCount == 0) {
            return response(trainingType, null, periodStart, periodEnd, completedCount, 3, NO_MONTHLY_COMPLETION, metrics);
        }
        if (completedCount < 3) {
            return response(trainingType, null, periodStart, periodEnd, completedCount, 3, INSUFFICIENT_COMPLETIONS, metrics);
        }
        return response(trainingType, scoreLevel(averageScore), periodStart, periodEnd, completedCount, 3, null, metrics);
    }

    private static TrainingLevelResponse documentLevel(
            TrainingType trainingType,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<MonthlyTrainingSummaryEntry> entries
    ) {
        int completedCount = entries.size();
        Integer highestCompletedLevel = entries.stream()
                .map(MonthlyTrainingSummaryEntry::sessionSubType)
                .map(TrainingProgressService::parseDocumentLevel)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        Map<String, Object> metrics = orderedMetrics();
        metrics.put("highestCompletedLevel", highestCompletedLevel);
        metrics.put("monthlyCompletedCount", completedCount);

        if (completedCount == 0) {
            return response(trainingType, null, periodStart, periodEnd, completedCount, 1, NO_MONTHLY_COMPLETION, metrics);
        }
        return response(trainingType, highestCompletedLevel, periodStart, periodEnd, completedCount, 1, null, metrics);
    }

    private static TrainingLevelResponse focusLevel(
            TrainingType trainingType,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<MonthlyTrainingSummaryEntry> entries
    ) {
        int completedCount = entries.size();
        Integer highestPlayedLevel = entries.stream()
                .map(MonthlyTrainingSummaryEntry::playedLevel)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        Map<String, Object> metrics = orderedMetrics();
        metrics.put("highestPlayedLevel", highestPlayedLevel);
        metrics.put("monthlyCompletedCount", completedCount);

        if (completedCount == 0) {
            return response(trainingType, null, periodStart, periodEnd, completedCount, 1, NO_MONTHLY_COMPLETION, metrics);
        }
        return response(trainingType, highestPlayedLevel, periodStart, periodEnd, completedCount, 1, null, metrics);
    }

    private static TrainingLevelResponse safetyLevel(
            TrainingType trainingType,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            List<MonthlyTrainingSummaryEntry> entries
    ) {
        int completedCount = entries.size();
        BigDecimal averageScore = averageScore(entries);
        List<String> coveredCategories = entries.stream()
                .map(MonthlyTrainingSummaryEntry::category)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        int categoryCap = categoryCap(coveredCategories.size());
        Map<String, Object> metrics = orderedMetrics();
        metrics.put("averageScore", averageScore);
        metrics.put("coveredCategoryCount", coveredCategories.size());
        metrics.put("coveredCategories", coveredCategories);
        metrics.put("monthlyCompletedCount", completedCount);

        if (completedCount == 0) {
            return response(trainingType, null, periodStart, periodEnd, completedCount, 3, NO_MONTHLY_COMPLETION, metrics);
        }
        if (completedCount < 3) {
            return response(trainingType, null, periodStart, periodEnd, completedCount, 3, INSUFFICIENT_COMPLETIONS, metrics);
        }

        Integer scoreLevel = scoreLevel(averageScore);
        Integer level = scoreLevel == null ? null : Math.min(scoreLevel, categoryCap);
        return response(trainingType, level, periodStart, periodEnd, completedCount, 3, null, metrics);
    }

    private static TrainingLevelResponse response(
            TrainingType trainingType,
            Integer level,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            int completedCount,
            int minRequiredCount,
            String reason,
            Map<String, Object> metrics
    ) {
        return new TrainingLevelResponse(
                trainingType,
                level,
                periodStart,
                periodEnd,
                TIMEZONE,
                completedCount,
                minRequiredCount,
                BASIS,
                reason,
                metrics
        );
    }

    private static BigDecimal averageScore(List<MonthlyTrainingSummaryEntry> entries) {
        List<Integer> scores = entries.stream()
                .map(MonthlyTrainingSummaryEntry::score)
                .filter(Objects::nonNull)
                .toList();
        if (scores.isEmpty()) {
            return null;
        }
        BigDecimal total = scores.stream()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);
    }

    private static Integer scoreLevel(BigDecimal averageScore) {
        if (averageScore == null) {
            return null;
        }
        if (averageScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return 5;
        }
        if (averageScore.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return 4;
        }
        if (averageScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return 3;
        }
        if (averageScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return 2;
        }
        return 1;
    }

    private static Integer parseDocumentLevel(String subType) {
        if (subType == null || !subType.startsWith("LEVEL_")) {
            return null;
        }
        try {
            int level = Integer.parseInt(subType.substring("LEVEL_".length()));
            return level >= 1 && level <= 5 ? level : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static int categoryCap(int coveredCategoryCount) {
        if (coveredCategoryCount >= 3) {
            return 5;
        }
        if (coveredCategoryCount == 2) {
            return 4;
        }
        return 2;
    }

    private static Map<String, Object> orderedMetrics() {
        return new LinkedHashMap<>();
    }

    private record MonthlyPeriod(LocalDateTime start, LocalDateTime end) {
    }
}
