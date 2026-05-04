package com.didgo.trainingservice.training.safety.repository;

import com.didgo.trainingservice.training.safety.dto.SafetyActionLogResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyActionDetailResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyChoiceResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyFeedbackResponse;
import com.didgo.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.didgo.trainingservice.training.safety.dto.SafetySceneResponse;
import com.didgo.trainingservice.training.safety.entity.SafetyCategory;
import java.util.List;
import java.util.Optional;

public interface SafetyTrainingRepository {

    List<SafetyScenarioListItemResponse> findActiveScenarios(SafetyCategory category);

    boolean existsActiveScenario(long scenarioId);

    Optional<SafetySceneResponse> findFirstScene(long scenarioId);

    Optional<SafetySceneResponse> findScene(long sceneId);

    List<SafetyChoiceResponse> findChoices(long sceneId);

    Optional<SafetyChoiceRow> findChoice(long sceneId, long choiceId);

    void saveActionLog(long sessionId, long sceneId, long choiceId, boolean correct);

    default Optional<SafetyScenarioSummaryRow> findScenarioSummaryBySessionId(long sessionId) {
        throw new UnsupportedOperationException("findScenarioSummaryBySessionId is not implemented.");
    }

    default SafetyActionSummaryRow summarizeActions(long sessionId) {
        throw new UnsupportedOperationException("summarizeActions is not implemented.");
    }

    List<SafetyActionLogResponse> findActionLogs(long sessionId);

    default Optional<SafetyScoreRow> findScore(long sessionId) {
        throw new UnsupportedOperationException("findScore is not implemented.");
    }

    default Optional<SafetyFeedbackResponse> findFeedback(long sessionId) {
        throw new UnsupportedOperationException("findFeedback is not implemented.");
    }

    default List<SafetyActionDetailResponse> findActionDetails(long sessionId) {
        throw new UnsupportedOperationException("findActionDetails is not implemented.");
    }

    record SafetyChoiceRow(
            long choiceId,
            long sceneId,
            Long nextSceneId,
            boolean correct,
            String resultText,
            String effectText,
            String feedbackImageUrl,
            String feedbackImageAlt
    ) {
        public SafetyChoiceRow(long choiceId, long sceneId, Long nextSceneId, boolean correct) {
            this(choiceId, sceneId, nextSceneId, correct, null, null, null, null);
        }

        public SafetyChoiceRow(long choiceId, long sceneId, Long nextSceneId, boolean correct, String resultText, String effectText) {
            this(choiceId, sceneId, nextSceneId, correct, resultText, effectText, null, null);
        }
    }

    record SafetyScenarioSummaryRow(long scenarioId, SafetyCategory category, String title) {
    }

    record SafetyActionSummaryRow(int correctCount, int totalCount) {
    }

    record SafetyScoreRow(int score, int correctCount, int totalCount) {
    }
}
