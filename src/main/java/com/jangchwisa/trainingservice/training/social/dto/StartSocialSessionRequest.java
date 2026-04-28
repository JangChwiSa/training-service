package com.jangchwisa.trainingservice.training.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record StartSocialSessionRequest(
        @NotBlank String jobType,
        @Positive long scenarioId
) {
}
