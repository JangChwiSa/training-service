package com.jangchwisa.trainingservice.event.publisher;

import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventBrokerPublisher implements EventBrokerPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventBrokerPublisher.class);

    @Override
    public void publish(OutboxEvent outboxEvent) {
        log.info(
                "Publishing event to broker placeholder. eventId={}, eventType={}, sessionId={}, trainingType={}",
                outboxEvent.eventId(),
                outboxEvent.eventType(),
                outboxEvent.sessionId(),
                outboxEvent.trainingType()
        );
    }
}
