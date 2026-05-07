package com.didgo.trainingservice.training.social.voice.dto;

public record SocialVoiceSessionPrepareResponse(
        long sessionId,
        long scenarioId,
        String connectionMode,
        RealtimeConnectionResponse realtime,
        OpeningVoiceResponse opening,
        ConversationSettingsResponse conversation
) {
}
