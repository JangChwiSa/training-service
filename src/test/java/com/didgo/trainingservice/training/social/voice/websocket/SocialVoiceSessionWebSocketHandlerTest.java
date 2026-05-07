package com.didgo.trainingservice.training.social.voice.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionToken;
import com.didgo.trainingservice.training.social.voice.realtime.OpenAiRealtimeProperties;
import com.didgo.trainingservice.training.social.voice.realtime.RealtimeUpstreamClient;
import com.didgo.trainingservice.training.social.voice.realtime.RealtimeUpstreamClientFactory;
import com.didgo.trainingservice.training.social.voice.realtime.RealtimeUpstreamEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

class SocialVoiceSessionWebSocketHandlerTest {

    ObjectMapper objectMapper = new ObjectMapper();
    FakeRealtimeUpstreamClientFactory upstreamFactory = new FakeRealtimeUpstreamClientFactory();
    SocialVoiceSessionWebSocketHandler handler = new SocialVoiceSessionWebSocketHandler(
            objectMapper,
            upstreamFactory,
            new OpenAiRealtimeProperties(null, "gpt-realtime-mini", "marin", "audio/pcm", 24000, "audio/pcm")
    );

    @Test
    void startsOpenAiRealtimeSessionAndRelaysAudioEvents() throws Exception {
        List<String> browserMessages = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SocialVoiceSessionHandshakeInterceptor.TOKEN_ATTRIBUTE, new SocialVoiceSessionToken(
                "token",
                10L,
                7L,
                1L,
                "복사 수량이 모호한 지시를 받은 상황",
                "안녕하세요. 같이 연습해볼게요.",
                "/static/opening/social/1/hash.pcm",
                Instant.parse("2026-04-30T01:05:00Z")
        ));
        WebSocketSession session = webSocketSession(attributes, browserMessages);

        handler.handleMessage(session, new TextMessage("{\"type\":\"session.start\",\"sessionId\":10}"));
        handler.handleMessage(session, new TextMessage("""
                {"type":"audio.chunk","sessionId":10,"chunkBase64":"AAAA","mimeType":"audio/pcm","sequence":1}
                """));
        handler.handleMessage(session, new TextMessage("{\"type\":\"audio.commit\",\"sessionId\":10}"));
        handler.handleMessage(session, new TextMessage("{\"type\":\"response.request\",\"sessionId\":10}"));
        upstreamFactory.eventHandler.onEvent("""
                {"type":"input_audio_buffer.committed"}
                """);
        upstreamFactory.eventHandler.onEvent("""
                {"type":"response.output_audio.delta","delta":"BBBB"}
                """);
        upstreamFactory.eventHandler.onEvent("""
                {"type":"response.output_audio_transcript.done","transcript":"좋아요."}
                """);

        assertThat(upstreamFactory.client.sentEvents)
                .anySatisfy(event -> {
                    JsonNode json = read(event);
                    assertThat(json.path("type").asText()).isEqualTo("session.update");
                    assertThat(json.path("session").path("output_modalities"))
                            .extracting(JsonNode::asText)
                            .containsExactly("audio");
                    assertThat(json.path("session").path("instructions").asText())
                            .contains("roleplaying as the other person")
                            .contains("복사 수량이 모호한 지시를 받은 상황")
                            .contains("안녕하세요. 같이 연습해볼게요.")
                            .contains("Speak only in Korean")
                            .contains("Do not act like a coach");
                })
                .anySatisfy(event -> {
                    JsonNode json = read(event);
                    assertThat(json.path("type").asText()).isEqualTo("input_audio_buffer.append");
                    assertThat(json.path("audio").asText()).isEqualTo("AAAA");
                })
                .anySatisfy(event -> assertThat(read(event).path("type").asText()).isEqualTo("input_audio_buffer.commit"))
                .anySatisfy(event -> {
                    JsonNode json = read(event);
                    assertThat(json.path("type").asText()).isEqualTo("response.create");
                    assertThat(json.path("response").path("output_modalities"))
                            .extracting(JsonNode::asText)
                            .containsExactly("audio");
                    assertThat(json.path("response").path("instructions").asText())
                            .contains("roleplaying as the other person")
                            .contains("안녕하세요. 같이 연습해볼게요.")
                            .contains("Speak only in Korean");
                });

        assertThat(browserMessages)
                .anySatisfy(message -> assertThat(read(message).path("type").asText()).isEqualTo("session.ready"))
                .anySatisfy(message -> assertThat(read(message).path("type").asText()).isEqualTo("opening.play"))
                .anySatisfy(message -> assertThat(read(message).path("type").asText()).isEqualTo("upstream.input_audio_buffer.committed"))
                .anySatisfy(message -> {
                    JsonNode json = read(message);
                    assertThat(json.path("type").asText()).isEqualTo("audio.out");
                    assertThat(json.path("chunkBase64").asText()).isEqualTo("BBBB");
                })
                .anySatisfy(message -> {
                    JsonNode json = read(message);
                    assertThat(json.path("type").asText()).isEqualTo("turn.complete");
                    assertThat(json.path("finalText").asText()).isEqualTo("좋아요.");
                });
    }

    @Test
    void returnsErrorWhenRealtimeProviderConnectionFails() throws Exception {
        List<String> browserMessages = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SocialVoiceSessionHandshakeInterceptor.TOKEN_ATTRIBUTE, new SocialVoiceSessionToken(
                "token",
                10L,
                7L,
                1L,
                "복사 수량이 모호한 지시를 받은 상황",
                "안녕하세요. 같이 연습해볼게요.",
                "/static/opening/social/1/hash.pcm",
                Instant.parse("2026-04-30T01:05:00Z")
        ));
        upstreamFactory.connectFailure = new IllegalStateException("missing api key");

        handler.handleMessage(webSocketSession(attributes, browserMessages),
                new TextMessage("{\"type\":\"session.start\",\"sessionId\":10}"));

        assertThat(browserMessages)
                .singleElement()
                .satisfies(message -> {
                    JsonNode json = read(message);
                    assertThat(json.path("type").asText()).isEqualTo("error");
                    assertThat(json.path("code").asText()).isEqualTo("REALTIME_PROVIDER_UNAVAILABLE");
                });
    }

    @Test
    void relaysResponseDoneAsDebugEventWithFinalTextFallback() throws Exception {
        List<String> browserMessages = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SocialVoiceSessionHandshakeInterceptor.TOKEN_ATTRIBUTE, new SocialVoiceSessionToken(
                "token",
                10L,
                7L,
                1L,
                "복사 수량이 모호한 지시를 받은 상황",
                "안녕하세요. 같이 연습해볼게요.",
                "/static/opening/social/1/hash.pcm",
                Instant.parse("2026-04-30T01:05:00Z")
        ));
        WebSocketSession session = webSocketSession(attributes, browserMessages);

        handler.handleMessage(session, new TextMessage("{\"type\":\"session.start\",\"sessionId\":10}"));
        upstreamFactory.eventHandler.onEvent("""
                {"type":"response.done","response":{"status":"completed","output":[{"content":[{"text":"복사는 10부로 해주세요."}]}]}}
                """);

        assertThat(browserMessages)
                .anySatisfy(message -> {
                    JsonNode json = read(message);
                    assertThat(json.path("type").asText()).isEqualTo("upstream.response.done");
                    assertThat(json.path("status").asText()).isEqualTo("completed");
                    assertThat(json.path("finalText").asText()).isEqualTo("복사는 10부로 해주세요.");
                });
    }

    @Test
    void rejectsResponseRequestWhenNoCommittedUserTurnExists() throws Exception {
        List<String> browserMessages = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SocialVoiceSessionHandshakeInterceptor.TOKEN_ATTRIBUTE, new SocialVoiceSessionToken(
                "token",
                10L,
                7L,
                1L,
                "복사 수량이 모호한 지시를 받은 상황",
                "안녕하세요. 같이 연습해볼게요.",
                "/static/opening/social/1/hash.pcm",
                Instant.parse("2026-04-30T01:05:00Z")
        ));
        WebSocketSession session = webSocketSession(attributes, browserMessages);

        handler.handleMessage(session, new TextMessage("{\"type\":\"session.start\",\"sessionId\":10}"));
        handler.handleMessage(session, new TextMessage("{\"type\":\"response.request\",\"sessionId\":10}"));

        assertThat(browserMessages)
                .anySatisfy(message -> {
                    JsonNode json = read(message);
                    assertThat(json.path("type").asText()).isEqualTo("error");
                    assertThat(json.path("code").asText()).isEqualTo("NO_USER_TURN_READY");
                });
    }

    @Test
    void acceptsResponseRequestImmediatelyAfterAudioCommit() throws Exception {
        List<String> browserMessages = new ArrayList<>();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SocialVoiceSessionHandshakeInterceptor.TOKEN_ATTRIBUTE, new SocialVoiceSessionToken(
                "token",
                10L,
                7L,
                1L,
                "복사 수량이 모호한 지시를 받은 상황",
                "안녕하세요. 같이 연습해볼게요.",
                "/static/opening/social/1/hash.pcm",
                Instant.parse("2026-04-30T01:05:00Z")
        ));
        WebSocketSession session = webSocketSession(attributes, browserMessages);

        handler.handleMessage(session, new TextMessage("{\"type\":\"session.start\",\"sessionId\":10}"));
        handler.handleMessage(session, new TextMessage("""
                {"type":"audio.chunk","sessionId":10,"chunkBase64":"AAAA","mimeType":"audio/pcm","sequence":1}
                """));
        handler.handleMessage(session, new TextMessage("{\"type\":\"audio.commit\",\"sessionId\":10}"));
        handler.handleMessage(session, new TextMessage("{\"type\":\"response.request\",\"sessionId\":10}"));

        assertThat(upstreamFactory.client.sentEvents)
                .anySatisfy(event -> assertThat(read(event).path("type").asText()).isEqualTo("response.create"));
    }

    private WebSocketSession webSocketSession(Map<String, Object> attributes, List<String> messages) {
        return new FakeWebSocketSession(attributes, messages);
    }

    private JsonNode read(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    static class FakeRealtimeUpstreamClientFactory implements RealtimeUpstreamClientFactory {

        FakeRealtimeUpstreamClient client = new FakeRealtimeUpstreamClient();
        RealtimeUpstreamEventHandler eventHandler;
        RuntimeException connectFailure;

        @Override
        public RealtimeUpstreamClient connect(
                SocialVoiceSessionToken token,
                RealtimeUpstreamEventHandler eventHandler
        ) {
            if (connectFailure != null) {
                throw connectFailure;
            }
            this.eventHandler = eventHandler;
            return client;
        }
    }

    static class FakeRealtimeUpstreamClient implements RealtimeUpstreamClient {

        List<String> sentEvents = new ArrayList<>();

        @Override
        public void send(String eventJson) {
            sentEvents.add(eventJson);
        }

        @Override
        public void close() {
        }
    }

    static class FakeWebSocketSession implements WebSocketSession {

        private final Map<String, Object> attributes;
        private final List<String> messages;
        private boolean open = true;
        private int textMessageSizeLimit;
        private int binaryMessageSizeLimit;

        FakeWebSocketSession(Map<String, Object> attributes, List<String> messages) {
            this.attributes = attributes;
            this.messages = messages;
        }

        @Override
        public String getId() {
            return "fake-session";
        }

        @Override
        public URI getUri() {
            return URI.create("ws://localhost/ws/trainings/social/voice");
        }

        @Override
        public HttpHeaders getHandshakeHeaders() {
            return HttpHeaders.EMPTY;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Principal getPrincipal() {
            return null;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public String getAcceptedProtocol() {
            return null;
        }

        @Override
        public void setTextMessageSizeLimit(int messageSizeLimit) {
            this.textMessageSizeLimit = messageSizeLimit;
        }

        @Override
        public int getTextMessageSizeLimit() {
            return textMessageSizeLimit;
        }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) {
            this.binaryMessageSizeLimit = messageSizeLimit;
        }

        @Override
        public int getBinaryMessageSizeLimit() {
            return binaryMessageSizeLimit;
        }

        @Override
        public List<WebSocketExtension> getExtensions() {
            return Collections.emptyList();
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) throws IOException {
            messages.add(message.getPayload().toString());
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }

        @Override
        public void close(org.springframework.web.socket.CloseStatus status) {
            open = false;
        }
    }
}
