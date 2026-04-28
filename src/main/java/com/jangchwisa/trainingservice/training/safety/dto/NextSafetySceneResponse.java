package com.jangchwisa.trainingservice.training.safety.dto;

public record NextSafetySceneResponse(
        SafetySelectedResultResponse selectedResult,
        SafetySceneResponse nextScene
) {
}
