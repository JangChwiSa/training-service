package com.didgo.trainingservice.training.social.voice.dto;

public record RealtimeConnectionResponse(
        String wsUrl,
        String protocol,
        String connectionToken,
        int expiresInSeconds
) {
}
