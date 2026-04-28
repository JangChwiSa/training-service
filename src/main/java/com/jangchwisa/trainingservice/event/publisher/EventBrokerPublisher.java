package com.jangchwisa.trainingservice.event.publisher;

import com.jangchwisa.trainingservice.event.outbox.OutboxEvent;

public interface EventBrokerPublisher {

    void publish(OutboxEvent outboxEvent);
}
