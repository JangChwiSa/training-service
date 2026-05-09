package com.didgo.trainingservice.training.social.voice.tts;

public record SocialOpeningAudioAsset(
        String cacheKey,
        long scenarioId,
        String script,
        String model,
        String voice,
        String responseFormat,
        String contentType,
        byte[] audioData
) {
}
