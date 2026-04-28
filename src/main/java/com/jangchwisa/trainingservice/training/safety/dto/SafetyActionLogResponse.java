package com.jangchwisa.trainingservice.training.safety.dto;

public record SafetyActionLogResponse(
        long sceneId,
        long choiceId,
        boolean correct
) {
}
