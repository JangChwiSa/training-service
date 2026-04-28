package com.jangchwisa.trainingservice.event.outbox;

import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOutboxEventRepository implements OutboxEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOutboxEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(OutboxEvent outboxEvent) {
        String sql = """
                INSERT INTO outbox_events (
                    event_id, event_type, aggregate_type, aggregate_id, session_id,
                    user_id, training_type, payload_json, status, retry_count,
                    max_retry_count, next_retry_at, published_at, last_error_message,
                    dlq_reason, created_at, updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                outboxEvent.eventId(),
                outboxEvent.eventType(),
                outboxEvent.aggregateType(),
                outboxEvent.aggregateId(),
                outboxEvent.sessionId(),
                outboxEvent.userId(),
                outboxEvent.trainingType().name(),
                outboxEvent.payloadJson(),
                outboxEvent.status(),
                outboxEvent.retryCount(),
                outboxEvent.maxRetryCount(),
                outboxEvent.nextRetryAt() == null ? null : Timestamp.valueOf(outboxEvent.nextRetryAt()),
                outboxEvent.publishedAt() == null ? null : Timestamp.valueOf(outboxEvent.publishedAt()),
                outboxEvent.lastErrorMessage(),
                outboxEvent.dlqReason(),
                Timestamp.valueOf(outboxEvent.createdAt()),
                Timestamp.valueOf(outboxEvent.updatedAt())
        );
    }
}
