package com.didgo.trainingservice.training.social.voice;

import java.time.Instant;

public record SocialVoiceSessionToken(
        String token,
        long sessionId,
        long userId,
        long scenarioId,
        String scenarioContext,
        String openingScript,
        String openingAudioUrl,
        Instant expiresAt
) {
}
