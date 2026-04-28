package com.jangchwisa.trainingservice.training.focus.dto;

import java.math.BigDecimal;

public record CompleteFocusSessionResponse(
        long sessionId,
        int score,
        BigDecimal accuracyRate,
        int wrongCount,
        int averageReactionMs,
        boolean unlockedNextLevel,
        int currentLevel,
        int highestUnlockedLevel
) {
}
