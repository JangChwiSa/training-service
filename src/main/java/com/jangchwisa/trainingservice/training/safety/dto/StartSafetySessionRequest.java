package com.jangchwisa.trainingservice.training.safety.dto;

import jakarta.validation.constraints.Positive;

public record StartSafetySessionRequest(
        @Positive long scenarioId
) {
}
