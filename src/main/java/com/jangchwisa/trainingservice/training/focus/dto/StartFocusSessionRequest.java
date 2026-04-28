package com.jangchwisa.trainingservice.training.focus.dto;

import jakarta.validation.constraints.Positive;

public record StartFocusSessionRequest(
        @Positive int level
) {
}
