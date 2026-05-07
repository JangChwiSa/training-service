package com.didgo.trainingservice.training.social.voice.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record VoiceSummaryRequest(
        String transcriptSource,
        String audioSessionId,
        @PositiveOrZero int durationSeconds
) {
}
