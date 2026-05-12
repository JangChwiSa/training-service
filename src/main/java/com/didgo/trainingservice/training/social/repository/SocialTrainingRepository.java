package com.didgo.trainingservice.training.social.repository;

import com.didgo.trainingservice.training.social.dto.SocialDialogLogRequest;
import com.didgo.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.didgo.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.didgo.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.service.SocialAdaptiveScenarioDraft;
import java.util.List;
import java.util.Optional;

public interface SocialTrainingRepository {

    List<SocialScenarioListItemResponse> findActiveScenariosByJobType(SocialJobType jobType);

    Optional<SocialScenarioDetailResponse> findActiveScenarioDetail(long scenarioId);

    default Optional<SocialScenarioDetailResponse> findAccessibleScenarioDetail(long scenarioId, long userId) {
        return findActiveScenarioDetail(scenarioId);
    }

    boolean existsActiveScenario(long scenarioId, SocialJobType jobType);

    default boolean existsAccessibleScenario(long scenarioId, SocialJobType jobType, long userId) {
        return existsActiveScenario(scenarioId, jobType);
    }

    default long saveGeneratedScenario(long userId, SocialJobType jobType, SocialAdaptiveScenarioDraft draft) {
        throw new UnsupportedOperationException("saveGeneratedScenario is not implemented.");
    }

    default List<SocialHistoryRow> findRecentSocialHistory(long userId, int limit) {
        return List.of();
    }

    default Optional<SocialAdaptiveRecommendationRow> findReadyAdaptiveRecommendation(long userId, SocialJobType jobType) {
        return Optional.empty();
    }

    default void saveAdaptiveRecommendation(long userId, SocialJobType jobType, long scenarioId) {
        throw new UnsupportedOperationException("saveAdaptiveRecommendation is not implemented.");
    }

    default void markAdaptiveRecommendationConsumed(long recommendationId) {
        throw new UnsupportedOperationException("markAdaptiveRecommendationConsumed is not implemented.");
    }

    default void upsertWeaknessProfile(
            long userId,
            SocialJobType jobType,
            long sourceSessionId,
            Integer recentScore,
            String weaknessLabel,
            String weaknessSummary
    ) {
        throw new UnsupportedOperationException("upsertWeaknessProfile is not implemented.");
    }

    default Optional<SocialWeaknessProfileRow> findWeaknessProfile(long userId, SocialJobType jobType) {
        return Optional.empty();
    }

    default Optional<SocialScenarioSummaryRow> findScenarioSummaryBySessionId(long sessionId) {
        throw new UnsupportedOperationException("findScenarioSummaryBySessionId is not implemented.");
    }

    default void saveDialogLogs(long sessionId, List<SocialDialogLogRequest> dialogLogs) {
        throw new UnsupportedOperationException("saveDialogLogs is not implemented.");
    }

    Optional<SocialScoreRow> findScore(long sessionId);

    Optional<SocialFeedbackResponse> findFeedback(long sessionId);

    List<SocialDialogLogResponse> findDialogLogs(long sessionId);

    record SocialScoreRow(int score, String scoreType) {
    }

    record SocialScenarioSummaryRow(long scenarioId, String title, SocialJobType jobType) {
        public SocialScenarioSummaryRow(long scenarioId, String title) {
            this(scenarioId, title, null);
        }
    }

    record SocialHistoryRow(
            long sessionId,
            String scenarioTitle,
            Integer score,
            String feedbackSummary,
            String feedbackDetail
    ) {
    }

    record SocialAdaptiveRecommendationRow(long recommendationId, long scenarioId, String focusSummary) {
    }

    record SocialWeaknessProfileRow(
            long userId,
            SocialJobType jobType,
            String weaknessLabel,
            String weaknessSummary,
            Long sourceSessionId,
            Integer recentScore
    ) {
    }
}
