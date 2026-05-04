package com.didgo.trainingservice.training.completion;

import java.math.BigDecimal;

public record TrainingCompletionProgress(
        Integer currentLevel,
        Integer highestUnlockedLevel,
        Integer playedLevel,
        BigDecimal accuracyRate,
        Integer averageReactionMs
) {

    public static TrainingCompletionProgress none() {
        return new TrainingCompletionProgress(null, null, null, null, null);
    }
}
