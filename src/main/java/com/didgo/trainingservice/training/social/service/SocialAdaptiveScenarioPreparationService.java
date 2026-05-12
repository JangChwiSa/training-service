package com.didgo.trainingservice.training.social.service;

import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository.SocialHistoryRow;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class SocialAdaptiveScenarioPreparationService {

    private static final Logger log = LoggerFactory.getLogger(SocialAdaptiveScenarioPreparationService.class);

    private final SocialTrainingRepository socialTrainingRepository;
    private final SocialAdaptiveScenarioGenerator socialAdaptiveScenarioGenerator;
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "social-adaptive-scenario-preparer");
        thread.setDaemon(true);
        return thread;
    });

    public SocialAdaptiveScenarioPreparationService(
            SocialTrainingRepository socialTrainingRepository,
            SocialAdaptiveScenarioGenerator socialAdaptiveScenarioGenerator,
            TransactionTemplate transactionTemplate
    ) {
        this.socialTrainingRepository = socialTrainingRepository;
        this.socialAdaptiveScenarioGenerator = socialAdaptiveScenarioGenerator;
        this.transactionTemplate = transactionTemplate;
    }

    public void prepareNextScenario(
            long userId,
            SocialJobType jobType,
            long sourceSessionId,
            Integer recentScore,
            String feedbackSummary,
            String feedbackDetail
    ) {
        if (jobType == null) {
            return;
        }
        executorService.submit(() -> prepareSafely(
                userId,
                jobType,
                sourceSessionId,
                recentScore,
                feedbackSummary,
                feedbackDetail
        ));
    }

    private void prepareSafely(
            long userId,
            SocialJobType jobType,
            long sourceSessionId,
            Integer recentScore,
            String feedbackSummary,
            String feedbackDetail
    ) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                WeaknessProfileDraft profile = weaknessProfile(recentScore, feedbackSummary, feedbackDetail);
                socialTrainingRepository.upsertWeaknessProfile(
                        userId,
                        jobType,
                        sourceSessionId,
                        recentScore,
                        profile.label(),
                        profile.summary()
                );

                if (socialTrainingRepository.findReadyAdaptiveRecommendation(userId, jobType).isPresent()) {
                    return;
                }

                List<SocialHistoryRow> historyRows = socialTrainingRepository.findRecentSocialHistory(userId, 8);
                SocialAdaptiveScenarioDraft draft = socialAdaptiveScenarioGenerator.generate(jobType, historyRows);
                long scenarioId = socialTrainingRepository.saveGeneratedScenario(userId, jobType, draft);
                socialTrainingRepository.saveAdaptiveRecommendation(userId, jobType, scenarioId);
            });
        } catch (RuntimeException exception) {
            log.warn("Failed to prepare social adaptive scenario. userId={}, jobType={}", userId, jobType, exception);
        }
    }

    private WeaknessProfileDraft weaknessProfile(Integer recentScore, String feedbackSummary, String feedbackDetail) {
        String source = normalize("%s %s".formatted(nullToEmpty(feedbackSummary), nullToEmpty(feedbackDetail)));
        String label;
        if (source.contains("거절")) {
            label = "정중한 거절 표현";
        } else if (source.contains("확인") || source.contains("모호") || source.contains("질문") || source.contains("구체")) {
            label = "모호한 지시 확인";
        } else if (source.contains("도움") || source.contains("요청")) {
            label = "도움 요청";
        } else if (recentScore != null && recentScore < 60) {
            label = "기본 응답 구조";
        } else {
            label = "상대 요청 확인";
        }

        String summary = "%s 연습이 필요합니다.".formatted(label);
        if (feedbackSummary != null && !feedbackSummary.isBlank()) {
            summary = shorten(feedbackSummary.trim(), 480);
        } else if (feedbackDetail != null && !feedbackDetail.isBlank()) {
            summary = shorten(feedbackDetail.trim(), 480);
        }
        return new WeaknessProfileDraft(label, summary);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String shorten(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdownNow();
    }

    private record WeaknessProfileDraft(String label, String summary) {
    }
}
