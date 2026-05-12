package com.didgo.trainingservice.training.social.service;

public record SocialAdaptiveScenarioDraft(
        String title,
        String backgroundText,
        String situationText,
        String characterInfo,
        String difficulty,
        String categoryCode,
        String evaluationPoint,
        String exampleAnswer,
        String focusSummary
) {
}
