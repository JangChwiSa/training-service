package com.didgo.trainingservice.training.social.dto;

import com.didgo.trainingservice.training.social.entity.SocialJobType;

public record SocialAdaptiveScenarioResponse(
        long scenarioId,
        SocialJobType jobType,
        String title,
        String backgroundText,
        String situationText,
        String characterInfo,
        Integer difficulty,
        String focusSummary
) {
}
