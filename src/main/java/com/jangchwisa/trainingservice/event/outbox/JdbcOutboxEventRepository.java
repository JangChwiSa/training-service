package com.jangchwisa.trainingservice.event.outbox;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOutboxEventRepository implements OutboxEventRepository {

    private static final RowMapper<OutboxEvent> ROW_MAPPER = (resultSet, rowNumber) -> new OutboxEvent(
            resultSet.getString("event_id"),
            resultSet.getString("event_type"),
            resultSet.getString("aggregate_type"),
            resultSet.getLong("aggregate_id"),
            resultSet.getLong("session_id"),
            resultSet.getLong("user_id"),
            com.jangchwisa.trainingservice.training.session.entity.TrainingType.valueOf(resultSet.getString("training_type")),
            resultSet.getString("payload_json"),
            resultSet.getString("status"),
            resultSet.getInt("retry_count"),
            resultSet.getInt("max_retry_count"),
            nullableDateTime(resultSet.getTimestamp("next_retry_at")),
            nullableDateTime(resultSet.getTimestamp("published_at")),
            resultSet.getString("last_error_message"),
            resultSet.getString("dlq_reason"),
            resultSet.getTimestamp("created_at").toLocalDateTime(),
            resultSet.getTimestamp("updated_at").toLocalDateTime()
    );

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

    @Override
    public List<OutboxEvent> findPublishableEvents(LocalDateTime now, int limit) {
        String sql = """
                SELECT event_id, event_type, aggregate_type, aggregate_id, session_id,
                       user_id, training_type, payload_json, status, retry_count,
                       max_retry_count, next_retry_at, published_at, last_error_message,
                       dlq_reason, created_at, updated_at
                FROM outbox_events
                WHERE status IN ('PENDING', 'RETRY')
                  AND (next_retry_at IS NULL OR next_retry_at <= ?)
                ORDER BY created_at ASC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER, Timestamp.valueOf(now), limit);
    }

    @Override
    public void markPublished(String eventId, LocalDateTime publishedAt) {
        String sql = """
                UPDATE outbox_events
                SET status = 'PUBLISHED',
                    published_at = ?,
                    updated_at = ?,
                    last_error_message = NULL,
                    dlq_reason = NULL
                WHERE event_id = ?
                """;
        jdbcTemplate.update(sql, Timestamp.valueOf(publishedAt), Timestamp.valueOf(publishedAt), eventId);
    }

    @Override
    public void markRetry(
            String eventId,
            int retryCount,
            LocalDateTime nextRetryAt,
            String lastErrorMessage,
            LocalDateTime updatedAt
    ) {
        String sql = """
                UPDATE outbox_events
                SET status = 'RETRY',
                    retry_count = ?,
                    next_retry_at = ?,
                    last_error_message = ?,
                    updated_at = ?
                WHERE event_id = ?
                """;
        jdbcTemplate.update(
                sql,
                retryCount,
                Timestamp.valueOf(nextRetryAt),
                lastErrorMessage,
                Timestamp.valueOf(updatedAt),
                eventId
        );
    }

    @Override
    public void markDlq(String eventId, String reason, String lastErrorMessage, LocalDateTime updatedAt) {
        String sql = """
                UPDATE outbox_events
                SET status = 'DLQ',
                    dlq_reason = ?,
                    last_error_message = ?,
                    updated_at = ?
                WHERE event_id = ?
                """;
        jdbcTemplate.update(sql, reason, lastErrorMessage, Timestamp.valueOf(updatedAt), eventId);
    }

    private static LocalDateTime nullableDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
