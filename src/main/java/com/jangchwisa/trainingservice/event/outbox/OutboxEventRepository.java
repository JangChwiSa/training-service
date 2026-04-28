package com.jangchwisa.trainingservice.event.outbox;

public interface OutboxEventRepository {

    void save(OutboxEvent outboxEvent);
}
