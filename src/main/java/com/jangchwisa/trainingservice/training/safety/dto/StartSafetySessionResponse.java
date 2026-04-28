package com.jangchwisa.trainingservice.training.safety.dto;

public record StartSafetySessionResponse(
        long sessionId,
        SafetySceneResponse scene
) {
}
