package com.didgo.trainingservice.external.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "training.openai")
public record OpenAiProperties(
        String apiKey,
        long timeoutMs,
        String adapter,
        String baseUrl,
        String model
) {

    public OpenAiProperties {
        apiKey = normalize(apiKey);
        adapter = normalize(adapter);
        baseUrl = normalize(baseUrl);
        model = normalize(model);
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("OpenAI timeout must be positive.");
        }
    }

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    public boolean hasModel() {
        return model != null && !model.isBlank();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
