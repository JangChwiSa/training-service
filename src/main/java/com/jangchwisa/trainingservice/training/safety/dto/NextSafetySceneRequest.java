package com.jangchwisa.trainingservice.training.safety.dto;

import jakarta.validation.constraints.Positive;

public record NextSafetySceneRequest(
        @Positive long sceneId,
        @Positive long choiceId
) {
}
