package com.jangchwisa.trainingservice.event.outbox;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository {

    void save(OutboxEvent outboxEvent);

    default List<OutboxEvent> findPublishableEvents(LocalDateTime now, int limit) {
        throw new UnsupportedOperationException("findPublishableEvents is not implemented.");
    }

    default void markPublished(String eventId, LocalDateTime publishedAt) {
        throw new UnsupportedOperationException("markPublished is not implemented.");
    }

    default void markRetry(
            String eventId,
            int retryCount,
            LocalDateTime nextRetryAt,
            String lastErrorMessage,
            LocalDateTime updatedAt
    ) {
        throw new UnsupportedOperationException("markRetry is not implemented.");
    }

    default void markDlq(String eventId, String reason, String lastErrorMessage, LocalDateTime updatedAt) {
        throw new UnsupportedOperationException("markDlq is not implemented.");
    }
}
