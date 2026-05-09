package com.didgo.trainingservice.training.social.voice.tts;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "training.openai.speech")
public record OpenAiSpeechProperties(
        String url,
        String model,
        String voice,
        String responseFormat,
        String instructions
) {
    private static final String DEFAULT_URL = "https://api.openai.com/v1/audio/speech";
    private static final String DEFAULT_MODEL = "gpt-4o-mini-tts";
    private static final String DEFAULT_VOICE = "marin";
    private static final String DEFAULT_RESPONSE_FORMAT = "mp3";
    private static final String DEFAULT_INSTRUCTIONS = "Speak Korean naturally as a busy workplace senior giving a short instruction. Keep it clear and realistic.";

    public OpenAiSpeechProperties {
        url = normalize(url, DEFAULT_URL);
        model = normalize(model, DEFAULT_MODEL);
        voice = normalize(voice, DEFAULT_VOICE);
        responseFormat = normalize(responseFormat, DEFAULT_RESPONSE_FORMAT);
        instructions = normalize(instructions, DEFAULT_INSTRUCTIONS);
    }

    public String contentType() {
        return switch (responseFormat) {
            case "wav" -> "audio/wav";
            case "opus" -> "audio/ogg";
            case "aac" -> "audio/aac";
            case "flac" -> "audio/flac";
            case "pcm" -> "audio/pcm";
            default -> "audio/mpeg";
        };
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
