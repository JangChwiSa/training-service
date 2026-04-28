package com.jangchwisa.trainingservice.training.safety.repository;

import com.jangchwisa.trainingservice.training.safety.dto.SafetyActionLogResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyChoiceResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetyScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.safety.dto.SafetySceneResponse;
import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
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

    record SafetyChoiceRow(long choiceId, long sceneId, Long nextSceneId, boolean correct) {
    }

    record SafetyScenarioSummaryRow(long scenarioId, SafetyCategory category, String title) {
    }

    record SafetyActionSummaryRow(int correctCount, int totalCount) {
    }
}
