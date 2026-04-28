package com.jangchwisa.trainingservice.training.focus.dto;

import java.math.BigDecimal;

public record FocusProgressResponse(
        int currentLevel,
        int highestUnlockedLevel,
        Integer lastPlayedLevel,
        BigDecimal lastAccuracyRate,
        Integer lastAverageReactionMs
) {
}
