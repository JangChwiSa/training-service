package com.jangchwisa.trainingservice.training.safety.dto;

import java.util.List;

public record SafetySceneResponse(
        long sceneId,
        String screenInfo,
        String situationText,
        String questionText,
        List<SafetyChoiceResponse> choices,
        boolean endScene
) {
}
