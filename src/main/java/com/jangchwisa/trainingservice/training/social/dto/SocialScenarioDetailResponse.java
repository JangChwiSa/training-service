package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;

public record SocialScenarioDetailResponse(
        long scenarioId,
        SocialJobType jobType,
        String title,
        String backgroundText,
        String situationText,
        String characterInfo,
        String difficulty
) {
}
