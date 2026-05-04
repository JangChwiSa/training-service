package com.didgo.trainingservice.event.publisher;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.didgo.trainingservice.event.outbox.OutboxEvent;
import com.didgo.trainingservice.event.outbox.OutboxEventRepository;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OutboxPublisherServiceTest {

    private static final Instant NOW = Instant.parse("2026-04-28T12:00:00Z");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW_LOCAL = LocalDateTime.ofInstant(NOW, ZONE_ID);

    private final FakeOutboxEventRepository outboxEventRepository = new FakeOutboxEventRepository();
    private final FakeEventBrokerPublisher eventBrokerPublisher = new FakeEventBrokerPublisher();
    private final OutboxPublisherProperties properties = properties();
    private final OutboxPublisherService service = new OutboxPublisherService(
            outboxEventRepository,
            eventBrokerPublisher,
            new ObjectMapper(),
            properties,
            Clock.fixed(NOW, ZONE_ID)
    );

    @Test
    void publishesDueEventAndMarksItPublished() {
        OutboxEvent event = event("evt-001", 0, 5, validPayload("evt-001"));
        outboxEventRepository.events = List.of(event);

        int publishedCount = service.publishDueEvents();

        assertThat(publishedCount).isEqualTo(1);
        assertThat(eventBrokerPublisher.publishedEvents).containsExactly(event);
        assertThat(outboxEventRepository.publishedEventId).isEqualTo("evt-001");
        assertThat(outboxEventRepository.publishedAt).isEqualTo(NOW_LOCAL);
    }

    @Test
    void marksRetryWhenPublishFailsBeforeMaxRetry() {
        OutboxEvent event = event("evt-001", 1, 5, validPayload("evt-001"));
        outboxEventRepository.events = List.of(event);
        eventBrokerPublisher.failure = new EventPublishException("broker unavailable");

        int publishedCount = service.publishDueEvents();

        assertThat(publishedCount).isZero();
        assertThat(outboxEventRepository.retryEventId).isEqualTo("evt-001");
        assertThat(outboxEventRepository.retryCount).isEqualTo(2);
        assertThat(outboxEventRepository.nextRetryAt).isEqualTo(NOW_LOCAL.plusSeconds(120));
        assertThat(outboxEventRepository.lastErrorMessage).isEqualTo("broker unavailable");
    }

    @Test
    void marksDlqWhenPublishFailsAtMaxRetry() {
        OutboxEvent event = event("evt-001", 4, 5, validPayload("evt-001"));
        outboxEventRepository.events = List.of(event);
        eventBrokerPublisher.failure = new EventPublishException("broker unavailable");

        service.publishDueEvents();

        assertThat(outboxEventRepository.dlqEventId).isEqualTo("evt-001");
        assertThat(outboxEventRepository.dlqReason).isEqualTo("MAX_RETRY_EXCEEDED");
        assertThat(outboxEventRepository.lastErrorMessage).isEqualTo("broker unavailable");
    }

    @Test
    void marksDlqWhenPayloadIsInvalid() {
        OutboxEvent event = event("evt-001", 0, 5, "{\"eventId\":\"different\"}");
        outboxEventRepository.events = List.of(event);

        service.publishDueEvents();

        assertThat(eventBrokerPublisher.publishedEvents).isEmpty();
        assertThat(outboxEventRepository.dlqEventId).isEqualTo("evt-001");
        assertThat(outboxEventRepository.dlqReason).isEqualTo("INVALID_PAYLOAD");
        assertThat(outboxEventRepository.lastErrorMessage).contains("Payload field is required");
    }

    @Test
    void capsRetryBackoffAtConfiguredMaximum() {
        OutboxEvent event = event("evt-001", 10, 20, validPayload("evt-001"));
        outboxEventRepository.events = List.of(event);
        eventBrokerPublisher.failure = new EventPublishException("broker unavailable");

        service.publishDueEvents();

        assertThat(outboxEventRepository.retryEventId).isEqualTo("evt-001");
        assertThat(outboxEventRepository.retryCount).isEqualTo(11);
        assertThat(outboxEventRepository.nextRetryAt).isEqualTo(NOW_LOCAL.plusSeconds(3600));
    }

    @Test
    void scheduledPublisherDoesNothingWhenDisabled() {
        OutboxPublisherProperties disabledProperties = properties();
        disabledProperties.setEnabled(false);
        OutboxPublisherService disabledService = new OutboxPublisherService(
                outboxEventRepository,
                eventBrokerPublisher,
                new ObjectMapper(),
                disabledProperties,
                Clock.fixed(NOW, ZONE_ID)
        );
        outboxEventRepository.events = List.of(event("evt-001", 0, 5, validPayload("evt-001")));

        disabledService.publishScheduled();

        assertThat(eventBrokerPublisher.publishedEvents).isEmpty();
        assertThat(outboxEventRepository.publishedEventId).isNull();
    }

    private OutboxPublisherProperties properties() {
        OutboxPublisherProperties properties = new OutboxPublisherProperties();
        properties.setBatchSize(10);
        properties.setRetryBackoffBaseSeconds(60);
        properties.setRetryBackoffMaxSeconds(3600);
        return properties;
    }

    private OutboxEvent event(String eventId, int retryCount, int maxRetryCount, String payloadJson) {
        return new OutboxEvent(
                eventId,
                "TrainingCompleted",
                "TrainingSession",
                10L,
                10L,
                1L,
                TrainingType.SOCIAL,
                payloadJson,
                retryCount == 0 ? "PENDING" : "RETRY",
                retryCount,
                maxRetryCount,
                null,
                null,
                null,
                null,
                NOW_LOCAL.minusMinutes(1),
                NOW_LOCAL.minusMinutes(1)
        );
    }

    private String validPayload(String eventId) {
        return """
                {"eventId":"%s","eventType":"TrainingCompleted","userId":1,"sessionId":10,"trainingType":"SOCIAL","score":85,"scoreType":"AI_EVALUATION","completedAt":"2026-04-28T21:00:00"}
                """.formatted(eventId).trim();
    }

    private static class FakeOutboxEventRepository implements OutboxEventRepository {

        private List<OutboxEvent> events = List.of();
        private String publishedEventId;
        private LocalDateTime publishedAt;
        private String retryEventId;
        private int retryCount;
        private LocalDateTime nextRetryAt;
        private String lastErrorMessage;
        private String dlqEventId;
        private String dlqReason;

        @Override
        public void save(OutboxEvent outboxEvent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<OutboxEvent> findPublishableEvents(LocalDateTime now, int limit) {
            return events.stream().limit(limit).toList();
        }

        @Override
        public void markPublished(String eventId, LocalDateTime publishedAt) {
            this.publishedEventId = eventId;
            this.publishedAt = publishedAt;
        }

        @Override
        public void markRetry(
                String eventId,
                int retryCount,
                LocalDateTime nextRetryAt,
                String lastErrorMessage,
                LocalDateTime updatedAt
        ) {
            this.retryEventId = eventId;
            this.retryCount = retryCount;
            this.nextRetryAt = nextRetryAt;
            this.lastErrorMessage = lastErrorMessage;
        }

        @Override
        public void markDlq(String eventId, String reason, String lastErrorMessage, LocalDateTime updatedAt) {
            this.dlqEventId = eventId;
            this.dlqReason = reason;
            this.lastErrorMessage = lastErrorMessage;
        }
    }

    private static class FakeEventBrokerPublisher implements EventBrokerPublisher {

        private final List<OutboxEvent> publishedEvents = new ArrayList<>();
        private RuntimeException failure;

        @Override
        public void publish(OutboxEvent outboxEvent) {
            if (failure != null) {
                throw failure;
            }
            publishedEvents.add(outboxEvent);
        }
    }
}
