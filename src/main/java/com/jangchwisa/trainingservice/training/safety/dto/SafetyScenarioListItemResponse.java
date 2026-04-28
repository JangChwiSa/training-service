package com.jangchwisa.trainingservice.training.safety.dto;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;

public record SafetyScenarioListItemResponse(
        long scenarioId,
        SafetyCategory category,
        String title,
        String description
) {
}
