package com.didgo.trainingservice.training.social.voice.realtime;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "training.openai.realtime")
public record OpenAiRealtimeProperties(
        String url,
        String model,
        String voice,
        String inputAudioFormat,
        int inputAudioRate,
        String outputAudioFormat
) {
    private static final String DEFAULT_URL = "wss://api.openai.com/v1/realtime";
    private static final String DEFAULT_MODEL = "gpt-realtime-mini";
    private static final String DEFAULT_VOICE = "marin";
    private static final String DEFAULT_AUDIO_FORMAT = "audio/pcm";
    private static final int DEFAULT_INPUT_AUDIO_RATE = 24000;

    public OpenAiRealtimeProperties {
        url = normalize(url, DEFAULT_URL);
        model = normalize(model, DEFAULT_MODEL);
        voice = normalize(voice, DEFAULT_VOICE);
        inputAudioFormat = normalize(inputAudioFormat, DEFAULT_AUDIO_FORMAT);
        outputAudioFormat = normalize(outputAudioFormat, DEFAULT_AUDIO_FORMAT);
        if (inputAudioRate <= 0) {
            inputAudioRate = DEFAULT_INPUT_AUDIO_RATE;
        }
    }

    private static String normalize(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
