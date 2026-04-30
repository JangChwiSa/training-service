package com.didgo.trainingservice.event.publisher;

import com.didgo.trainingservice.event.outbox.OutboxEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HttpEventBrokerPublisher implements EventBrokerPublisher {

    private final EventBrokerProperties properties;
    private final HttpClient httpClient;

    @Autowired
    public HttpEventBrokerPublisher(EventBrokerProperties properties) {
        this(properties, HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.timeoutMs()))
                .build());
    }

    HttpEventBrokerPublisher(EventBrokerProperties properties, HttpClient httpClient) {
        this.properties = properties;
        this.httpClient = httpClient;
    }

    @Override
    public void publish(OutboxEvent outboxEvent) {
        if (!properties.hasUrl()) {
            throw new EventPublishException("Event broker URL is not configured.");
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.url()))
                    .timeout(Duration.ofMillis(properties.timeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("X-Event-Type", outboxEvent.eventType())
                    .header("X-Event-Id", outboxEvent.eventId())
                    .POST(HttpRequest.BodyPublishers.ofString(outboxEvent.payloadJson()))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EventPublishException("Event broker publish failed with status " + response.statusCode() + ".");
            }
        } catch (IOException exception) {
            throw new EventPublishException("Event broker publish failed.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Event broker publish was interrupted.", exception);
        }
    }
}
