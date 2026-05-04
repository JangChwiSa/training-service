package com.didgo.trainingservice.event.publisher;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "training.outbox.publisher")
public class OutboxPublisherProperties {

    private boolean enabled;
    private int batchSize = 50;
    private long fixedDelayMs = 5000;
    private long retryBackoffBaseSeconds = 60;
    private long retryBackoffMaxSeconds = 3600;

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int batchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long fixedDelayMs() {
        return fixedDelayMs;
    }

    public void setFixedDelayMs(long fixedDelayMs) {
        this.fixedDelayMs = fixedDelayMs;
    }

    public long retryBackoffBaseSeconds() {
        return retryBackoffBaseSeconds;
    }

    public void setRetryBackoffBaseSeconds(long retryBackoffBaseSeconds) {
        this.retryBackoffBaseSeconds = retryBackoffBaseSeconds;
    }

    public long retryBackoffMaxSeconds() {
        return retryBackoffMaxSeconds;
    }

    public void setRetryBackoffMaxSeconds(long retryBackoffMaxSeconds) {
        this.retryBackoffMaxSeconds = retryBackoffMaxSeconds;
    }
}
