package com.didgo.trainingservice.event.publisher;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "training.event-broker")
public record EventBrokerProperties(
        String url,
        long timeoutMs
) {

    public EventBrokerProperties {
        url = normalize(url);
        if (timeoutMs <= 0) {
            timeoutMs = 5000;
        }
    }

    public boolean hasUrl() {
        return url != null && !url.isBlank();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
