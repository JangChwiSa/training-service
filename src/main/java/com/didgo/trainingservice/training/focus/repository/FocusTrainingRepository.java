package com.didgo.trainingservice.training.focus.repository;

import com.didgo.trainingservice.training.focus.dto.FocusReactionRequest;
import com.didgo.trainingservice.training.focus.dto.FocusCommandResponse;
import com.didgo.trainingservice.training.focus.dto.FocusProgressResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FocusTrainingRepository {

    Optional<FocusProgressResponse> findProgress(long userId);

    Optional<FocusLevelRuleRow> findActiveLevelRule(int level);

    default Optional<Integer> findSessionLevel(long sessionId) {
        throw new UnsupportedOperationException("findSessionLevel is not implemented.");
    }

    List<FocusCommandResponse> saveCommands(long sessionId, List<NewFocusCommand> commands);

    default List<FocusCommandRow> findCommands(long sessionId) {
        throw new UnsupportedOperationException("findCommands is not implemented.");
    }

    default void saveReactionLogs(long sessionId, List<ScoredFocusReaction> reactions) {
        throw new UnsupportedOperationException("saveReactionLogs is not implemented.");
    }

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

    record FocusCommandRow(
            long commandId,
            int commandOrder,
            String commandText,
            String expectedAction
    ) {
    }

    record ScoredFocusReaction(
            long commandId,
            String userInput,
            boolean correct,
            int reactionMs
    ) {

        public static ScoredFocusReaction from(FocusReactionRequest request, boolean correct) {
            return new ScoredFocusReaction(request.commandId(), request.userInput(), correct, request.reactionMs());
        }
    }
}
