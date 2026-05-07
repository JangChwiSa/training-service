package com.didgo.trainingservice.training.social.voice.dto;

public record ConversationSettingsResponse(
        String voice,
        String model,
        String instructionsVersion
) {
}
