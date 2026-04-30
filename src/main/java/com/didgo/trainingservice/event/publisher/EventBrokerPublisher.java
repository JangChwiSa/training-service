package com.didgo.trainingservice.event.publisher;

import com.didgo.trainingservice.event.outbox.OutboxEvent;

public interface EventBrokerPublisher {

    void publish(OutboxEvent outboxEvent);
}
