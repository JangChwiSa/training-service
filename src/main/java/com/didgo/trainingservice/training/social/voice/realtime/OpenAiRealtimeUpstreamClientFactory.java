package com.didgo.trainingservice.training.social.voice.realtime;

import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionToken;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.springframework.stereotype.Component;

@Component
public class OpenAiRealtimeUpstreamClientFactory implements RealtimeUpstreamClientFactory {

    private final OpenAiProperties openAiProperties;
    private final OpenAiRealtimeProperties realtimeProperties;
    private final HttpClient httpClient;

    public OpenAiRealtimeUpstreamClientFactory(
            OpenAiProperties openAiProperties,
            OpenAiRealtimeProperties realtimeProperties
    ) {
        this.openAiProperties = openAiProperties;
        this.realtimeProperties = realtimeProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(openAiProperties.timeoutMs()))
                .build();
    }

    @Override
    public RealtimeUpstreamClient connect(
            SocialVoiceSessionToken token,
            RealtimeUpstreamEventHandler eventHandler
    ) {
        validateConfiguration();
        URI uri = URI.create(realtimeProperties.url()
                + "?model="
                + URLEncoder.encode(realtimeProperties.model(), StandardCharsets.UTF_8));
        WebSocket webSocket = httpClient.newWebSocketBuilder()
                .header("Authorization", "Bearer " + openAiProperties.apiKey())
                .connectTimeout(Duration.ofMillis(openAiProperties.timeoutMs()))
                .buildAsync(uri, new Listener(eventHandler))
                .join();
        return new JavaNetRealtimeUpstreamClient(webSocket);
    }

    private void validateConfiguration() {
        if (!openAiProperties.hasApiKey()) {
            throw new IllegalStateException("OPENAI_API_KEY is required for Realtime voice relay.");
        }
    }

    private static class JavaNetRealtimeUpstreamClient implements RealtimeUpstreamClient {

        private final WebSocket webSocket;

        JavaNetRealtimeUpstreamClient(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        @Override
        public void send(String eventJson) {
            webSocket.sendText(eventJson, true).join();
        }

        @Override
        public void close() {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "client session closed").join();
        }
    }

    private static class Listener implements WebSocket.Listener {

        private final RealtimeUpstreamEventHandler eventHandler;
        private final StringBuilder buffer = new StringBuilder();

        Listener(RealtimeUpstreamEventHandler eventHandler) {
            this.eventHandler = eventHandler;
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                eventHandler.onEvent(buffer.toString());
                buffer.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            eventHandler.onError(error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            eventHandler.onClose(statusCode, reason);
            return CompletableFuture.completedFuture(null);
        }
    }
}
