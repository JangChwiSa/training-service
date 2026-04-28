package com.jangchwisa.trainingservice.training.focus.repository;

import com.jangchwisa.trainingservice.training.focus.dto.FocusCommandResponse;
import com.jangchwisa.trainingservice.training.focus.dto.FocusProgressResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FocusTrainingRepository {

    Optional<FocusProgressResponse> findProgress(long userId);

    Optional<FocusLevelRuleRow> findActiveLevelRule(int level);

    List<FocusCommandResponse> saveCommands(long sessionId, List<NewFocusCommand> commands);

    record FocusLevelRuleRow(
            int level,
            int durationSeconds,
            int commandIntervalMs,
            String commandComplexity,
            BigDecimal requiredAccuracyRate
    ) {
    }

    record NewFocusCommand(
            int order,
            String commandText,
            String expectedAction,
            int displayAtMs
    ) {
    }
}
