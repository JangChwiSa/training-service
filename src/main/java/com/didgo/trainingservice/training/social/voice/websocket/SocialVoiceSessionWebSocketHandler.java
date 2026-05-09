package com.didgo.trainingservice.training.social.voice.websocket;

import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionToken;
import com.didgo.trainingservice.training.social.voice.realtime.OpenAiRealtimeProperties;
import com.didgo.trainingservice.training.social.voice.realtime.RealtimeUpstreamClient;
import com.didgo.trainingservice.training.social.voice.realtime.RealtimeUpstreamClientFactory;
import com.didgo.trainingservice.training.social.voice.realtime.RealtimeUpstreamEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class SocialVoiceSessionWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SocialVoiceSessionWebSocketHandler.class);
    private static final List<String> AUDIO_ONLY_MODALITIES = List.of("audio");
    private static final String TYPE_SESSION_START = "session.start";
    private static final String TYPE_AUDIO_CHUNK = "audio.chunk";
    private static final String TYPE_AUDIO_COMMIT = "audio.commit";
    private static final String TYPE_RESPONSE_REQUEST = "response.request";
    private static final String TYPE_SESSION_FINISH = "session.finish";
    private static final String UPSTREAM_ATTRIBUTE = "openAiRealtimeUpstream";
    private static final String TURN_COUNTER_ATTRIBUTE = "turnCounter";
    private static final String AUDIO_BUFFERED_ATTRIBUTE = "audioBuffered";
    private static final String AUDIO_CHUNK_COUNTER_ATTRIBUTE = "audioChunkCounter";
    private static final String OUTPUT_AUDIO_CHUNK_COUNTER_ATTRIBUTE = "outputAudioChunkCounter";
    private static final String USER_TURN_READY_ATTRIBUTE = "userTurnReady";
    private static final String UPSTREAM_AUDIO_COMMITTED_ATTRIBUTE = "upstreamAudioCommitted";
    private static final String PENDING_RESPONSE_REQUEST_ATTRIBUTE = "pendingResponseRequest";
    private static final String UPSTREAM_RESPONSE_DONE_ATTRIBUTE = "upstreamResponseDone";

    private final ObjectMapper objectMapper;
    private final RealtimeUpstreamClientFactory upstreamClientFactory;
    private final OpenAiRealtimeProperties realtimeProperties;

    public SocialVoiceSessionWebSocketHandler(
            ObjectMapper objectMapper,
            RealtimeUpstreamClientFactory upstreamClientFactory,
            OpenAiRealtimeProperties realtimeProperties
    ) {
        this.objectMapper = objectMapper;
        this.upstreamClientFactory = upstreamClientFactory;
        this.realtimeProperties = realtimeProperties;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SocialVoiceSessionToken token = token(session);
        JsonNode payload = objectMapper.readTree(message.getPayload());
        String type = payload.path("type").asText();

        if (TYPE_SESSION_START.equals(type)) {
            handleSessionStart(session, payload, token);
            return;
        }
        if (TYPE_AUDIO_CHUNK.equals(type)) {
            handleAudioChunk(session, payload, token);
            return;
        }
        if (TYPE_AUDIO_COMMIT.equals(type)) {
            if (!audioBuffered(session)) {
                send(session, Map.of(
                        "type", "error",
                        "sessionId", token.sessionId(),
                        "code", "EMPTY_AUDIO_INPUT",
                        "message", "사용자 음성이 아직 입력되지 않았습니다."
                ));
                return;
            }
            upstream(session).send(objectMapper.writeValueAsString(Map.of("type", "input_audio_buffer.commit")));
            session.getAttributes().put(AUDIO_BUFFERED_ATTRIBUTE, false);
            session.getAttributes().put(USER_TURN_READY_ATTRIBUTE, true);
            session.getAttributes().put(UPSTREAM_AUDIO_COMMITTED_ATTRIBUTE, false);
            log.info("Social voice audio committed. sessionId={}", token.sessionId());
            return;
        }
        if (TYPE_RESPONSE_REQUEST.equals(type)) {
            if (!userTurnReady(session)) {
                log.info("Social voice response request rejected before commit. sessionId={}", token.sessionId());
                send(session, Map.of(
                        "type", "error",
                        "sessionId", token.sessionId(),
                        "code", "NO_USER_TURN_READY",
                        "message", "응답을 생성하려면 먼저 사용자 음성을 녹음하고 commit 해야 합니다."
                ));
                return;
            }
            if (!upstreamAudioCommitted(session)) {
                session.getAttributes().put(PENDING_RESPONSE_REQUEST_ATTRIBUTE, true);
                log.info("Social voice response request queued until upstream commit. sessionId={}", token.sessionId());
                return;
            }
            requestUpstreamResponse(session, token);
            return;
        }
        if (TYPE_SESSION_FINISH.equals(type)) {
            upstream(session).close();
            send(session, Map.of("type", "session.completed", "sessionId", token.sessionId(), "status", "COMPLETED"));
            session.close(CloseStatus.NORMAL);
            return;
        }

        send(session, Map.of(
                "type", "error",
                "sessionId", token.sessionId(),
                "code", "UNSUPPORTED_EVENT",
                "message", "Unsupported voice session event."
        ));
    }

    private void handleSessionStart(
            WebSocketSession session,
            JsonNode payload,
            SocialVoiceSessionToken token
    ) throws IOException {
        long requestedSessionId = payload.path("sessionId").asLong(-1);
        if (requestedSessionId != token.sessionId()) {
            send(session, Map.of(
                    "type", "error",
                    "sessionId", token.sessionId(),
                    "code", "SESSION_MISMATCH",
                    "message", "Session id does not match the connection token."
            ));
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        RealtimeUpstreamClient upstream;
        try {
            upstream = upstreamClientFactory.connect(token, new BrowserRelayEventHandler(session, token));
        } catch (RuntimeException exception) {
            send(session, Map.of(
                    "type", "error",
                    "sessionId", token.sessionId(),
                    "code", "REALTIME_PROVIDER_UNAVAILABLE",
                    "message", "실시간 음성 연결에 실패했습니다."
            ));
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }
        session.getAttributes().put(UPSTREAM_ATTRIBUTE, upstream);
        session.getAttributes().put(TURN_COUNTER_ATTRIBUTE, new AtomicInteger(1));
        session.getAttributes().put(AUDIO_CHUNK_COUNTER_ATTRIBUTE, new AtomicInteger(0));
        session.getAttributes().put(OUTPUT_AUDIO_CHUNK_COUNTER_ATTRIBUTE, new AtomicInteger(0));
        session.getAttributes().put(AUDIO_BUFFERED_ATTRIBUTE, false);
        session.getAttributes().put(USER_TURN_READY_ATTRIBUTE, false);
        session.getAttributes().put(UPSTREAM_AUDIO_COMMITTED_ATTRIBUTE, false);
        session.getAttributes().put(PENDING_RESPONSE_REQUEST_ATTRIBUTE, false);
        session.getAttributes().put(UPSTREAM_RESPONSE_DONE_ATTRIBUTE, false);
        upstream.send(sessionUpdate(token));
        log.info("Social voice realtime session started. sessionId={}", token.sessionId());

        send(session, Map.of(
                "type", "session.ready",
                "sessionId", token.sessionId(),
                "opening", Map.of(
                        "script", token.openingScript(),
                        "audioUrl", token.openingAudioUrl()
                )
        ));
        send(session, Map.of(
                "type", "opening.play",
                "sessionId", token.sessionId(),
                "script", token.openingScript()
        ));
    }

    private void handleAudioChunk(WebSocketSession session, JsonNode payload, SocialVoiceSessionToken token) throws IOException {
        String mimeType = payload.path("mimeType").asText("audio/pcm");
        if (!realtimeProperties.inputAudioFormat().equals(mimeType)) {
            send(session, Map.of(
                    "type", "error",
                    "sessionId", token.sessionId(),
                    "code", "UNSUPPORTED_AUDIO_FORMAT",
                    "message", "Only " + realtimeProperties.inputAudioFormat() + " is supported."
            ));
            return;
        }
        String chunkBase64 = payload.path("chunkBase64").asText(null);
        if (chunkBase64 == null || chunkBase64.isBlank()) {
            send(session, Map.of(
                    "type", "error",
                    "sessionId", token.sessionId(),
                    "code", "INVALID_AUDIO_CHUNK",
                    "message", "Audio chunk is required."
            ));
            return;
        }
        upstream(session).send(objectMapper.writeValueAsString(Map.of(
                "type", "input_audio_buffer.append",
                "audio", chunkBase64
        )));
        session.getAttributes().put(AUDIO_BUFFERED_ATTRIBUTE, true);
        session.getAttributes().put(UPSTREAM_AUDIO_COMMITTED_ATTRIBUTE, false);
        int chunkCount = ((AtomicInteger) session.getAttributes().get(AUDIO_CHUNK_COUNTER_ATTRIBUTE)).incrementAndGet();
        if (chunkCount == 1 || chunkCount % 25 == 0) {
            log.info("Social voice audio chunk relayed. sessionId={}, chunkCount={}, bytesBase64={}",
                    token.sessionId(), chunkCount, chunkBase64.length());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object tokenAttribute = session.getAttributes().get(SocialVoiceSessionHandshakeInterceptor.TOKEN_ATTRIBUTE);
        if (tokenAttribute instanceof SocialVoiceSessionToken token) {
            log.info("Social voice browser websocket closed. sessionId={}, statusCode={}, reason={}",
                    token.sessionId(), status.getCode(), status.getReason());
        } else {
            log.info("Social voice browser websocket closed before token resolution. statusCode={}, reason={}",
                    status.getCode(), status.getReason());
        }

        Object upstream = session.getAttributes().get(UPSTREAM_ATTRIBUTE);
        if (upstream instanceof RealtimeUpstreamClient upstreamClient) {
            upstreamClient.close();
        }
    }

    private SocialVoiceSessionToken token(WebSocketSession session) throws IOException {
        Object token = session.getAttributes().get(SocialVoiceSessionHandshakeInterceptor.TOKEN_ATTRIBUTE);
        if (token instanceof SocialVoiceSessionToken sessionToken) {
            return sessionToken;
        }
        session.close(CloseStatus.POLICY_VIOLATION);
        throw new IOException("Missing social voice session token.");
    }

    private RealtimeUpstreamClient upstream(WebSocketSession session) throws IOException {
        Object upstream = session.getAttributes().get(UPSTREAM_ATTRIBUTE);
        if (upstream instanceof RealtimeUpstreamClient upstreamClient) {
            return upstreamClient;
        }
        session.close(CloseStatus.POLICY_VIOLATION);
        throw new IOException("OpenAI Realtime upstream is not connected.");
    }

    private void send(WebSocketSession session, Map<String, Object> payload) throws IOException {
        synchronized (session) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        }
    }

    private String sessionUpdate(SocialVoiceSessionToken token) throws IOException {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("format", Map.of(
                "type", realtimeProperties.inputAudioFormat(),
                "rate", realtimeProperties.inputAudioRate()
        ));
        input.put("turn_detection", null);
        input.put("transcription", Map.of(
                "model", "gpt-4o-mini-transcribe",
                "language", "ko"
        ));

        Map<String, Object> audio = new LinkedHashMap<>();
        audio.put("input", input);
        audio.put("output", Map.of(
                "format", Map.of(
                        "type", realtimeProperties.outputAudioFormat(),
                        "rate", realtimeProperties.inputAudioRate()
                ),
                "voice", realtimeProperties.voice()
        ));

        Map<String, Object> realtimeSession = new LinkedHashMap<>();
        realtimeSession.put("type", "realtime");
        realtimeSession.put("model", realtimeProperties.model());
        realtimeSession.put("output_modalities", AUDIO_ONLY_MODALITIES);
        realtimeSession.put("audio", audio);
        realtimeSession.put("instructions", instructions(token));

        Map<String, Object> event = new HashMap<>();
        event.put("type", "session.update");
        event.put("session", realtimeSession);
        return objectMapper.writeValueAsString(event);
    }

    private String responseCreate(SocialVoiceSessionToken token) throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("instructions", instructions(token));
        response.put("output_modalities", AUDIO_ONLY_MODALITIES);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "response.create");
        event.put("response", response);
        return objectMapper.writeValueAsString(event);
    }

    private String instructions(SocialVoiceSessionToken token) {
        return """
                You are roleplaying as the other person in a Korean workplace training scenario.
                Stay fully in character and respond as if the situation is happening right now.
                Scenario: %s
                Scene opening already given to the user: %s
                The conversation starts immediately after that opening. Stay inside this exact scene.

                Rules:
                - Speak only in Korean unless the user explicitly asks you to switch languages.
                - Do not use English for the default reply.
                - Do not act like a coach, teacher, counselor, or narrator.
                - Do not explain what the user should do.
                - Do not give strategy, feedback, or example answers unless the user explicitly asks to stop roleplay.
                - Speak to the user as the real counterpart in the scene, such as a boss, coworker, customer, or interviewer.
                - If the scenario involves a senior coworker, supervisor, boss, or a person giving work instructions, act as that senior workplace person, not as a peer helper.
                - In this scene, you are the person who gave or owns the opening request. Maintain that authority and practical workplace concern.
                - Keep replies short, natural, and conversational, usually 1 to 3 sentences.
                - If the user asks a clarification question, answer it in character based on the scenario.
                - If the user refuses, avoids the task, or says they do not want to do it, do not solve the task for them.
                - For refusal or avoidance, respond like the senior workplace counterpart: acknowledge briefly, state why the task is needed now, and ask what part is difficult or what information they need.
                - Do not say what "we" or "I" should do as an alternative plan unless the counterpart in the scene would realistically take over the task.
                - The browser already played an opening narration for the practice, so do not repeat the narration or mention that this is practice.
                - If the user says something vague, respond as the counterpart in this scene, not as a generic assistant greeting.
                """.formatted(token.scenarioContext(), token.openingScript());
    }

    private boolean audioBuffered(WebSocketSession session) {
        return Boolean.TRUE.equals(session.getAttributes().get(AUDIO_BUFFERED_ATTRIBUTE));
    }

    private boolean userTurnReady(WebSocketSession session) {
        return Boolean.TRUE.equals(session.getAttributes().get(USER_TURN_READY_ATTRIBUTE));
    }

    private boolean upstreamAudioCommitted(WebSocketSession session) {
        return Boolean.TRUE.equals(session.getAttributes().get(UPSTREAM_AUDIO_COMMITTED_ATTRIBUTE));
    }

    private boolean pendingResponseRequest(WebSocketSession session) {
        return Boolean.TRUE.equals(session.getAttributes().get(PENDING_RESPONSE_REQUEST_ATTRIBUTE));
    }

    private void requestUpstreamResponse(WebSocketSession session, SocialVoiceSessionToken token) throws IOException {
        upstream(session).send(responseCreate(token));
        session.getAttributes().put(USER_TURN_READY_ATTRIBUTE, false);
        session.getAttributes().put(PENDING_RESPONSE_REQUEST_ATTRIBUTE, false);
        session.getAttributes().put(UPSTREAM_RESPONSE_DONE_ATTRIBUTE, false);
        log.info("Social voice response requested. sessionId={}", token.sessionId());
    }

    private class BrowserRelayEventHandler implements RealtimeUpstreamEventHandler {

        private final WebSocketSession browserSession;
        private final SocialVoiceSessionToken token;

        BrowserRelayEventHandler(WebSocketSession browserSession, SocialVoiceSessionToken token) {
            this.browserSession = browserSession;
            this.token = token;
        }

        @Override
        public void onEvent(String eventJson) {
            try {
                JsonNode event = objectMapper.readTree(eventJson);
                logUpstreamEvent(event);
                relayOpenAiEvent(event);
            } catch (IOException exception) {
                onError(exception);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            log.warn("Social voice realtime upstream error. sessionId={}", token.sessionId(), throwable);
            try {
                send(browserSession, Map.of(
                        "type", "error",
                        "sessionId", token.sessionId(),
                        "code", "REALTIME_PROVIDER_UNAVAILABLE",
                        "message", "실시간 음성 연결에 실패했습니다."
                ));
            } catch (IOException ignored) {
            }
        }

        @Override
        public void onClose(int statusCode, String reason) {
            log.info("Social voice realtime upstream closed. sessionId={}, statusCode={}, reason={}",
                    token.sessionId(), statusCode, reason);
            try {
                if (Boolean.TRUE.equals(browserSession.getAttributes().get(UPSTREAM_RESPONSE_DONE_ATTRIBUTE))) {
                    send(browserSession, Map.of(
                            "type", "session.completed",
                            "sessionId", token.sessionId(),
                            "status", "CLOSED"
                    ));
                    return;
                }

                send(browserSession, Map.of(
                        "type", "error",
                        "sessionId", token.sessionId(),
                        "code", "REALTIME_PROVIDER_CLOSED",
                        "message", "실시간 음성 연결이 종료되었습니다. 잠시 후 다시 시도해 주세요.",
                        "providerStatusCode", statusCode,
                        "providerReason", reason == null ? "" : reason
                ));
            } catch (IOException ignored) {
            }
        }

        private void relayOpenAiEvent(JsonNode event) throws IOException {
            String type = event.path("type").asText();
            switch (type) {
                case "input_audio_buffer.committed" -> {
                    browserSession.getAttributes().put(UPSTREAM_AUDIO_COMMITTED_ATTRIBUTE, true);
                    browserSession.getAttributes().put(USER_TURN_READY_ATTRIBUTE, true);
                    send(browserSession, Map.of(
                            "type", "upstream.input_audio_buffer.committed",
                            "sessionId", token.sessionId()
                    ));
                    if (pendingResponseRequest(browserSession)) {
                        requestUpstreamResponse(browserSession, token);
                    }
                }
                case "response.created" -> send(browserSession, Map.of(
                        "type", "upstream.response.created",
                        "sessionId", token.sessionId(),
                        "responseId", event.path("response").path("id").asText("")
                ));
                case "response.output_audio.delta" -> send(browserSession, Map.of(
                        "type", "audio.out",
                        "sessionId", token.sessionId(),
                        "turnNo", currentTurn(),
                        "chunkBase64", event.path("delta").asText(),
                        "mimeType", realtimeProperties.outputAudioFormat()
                ));
                case "response.output_audio_transcript.delta" -> send(browserSession, Map.of(
                        "type", "transcript.partial",
                        "sessionId", token.sessionId(),
                        "speaker", "AI",
                        "turnNo", currentTurn(),
                        "text", event.path("delta").asText()
                ));
                case "response.output_audio_transcript.done" -> send(browserSession, Map.of(
                        "type", "turn.complete",
                        "sessionId", token.sessionId(),
                        "turnNo", incrementTurn(),
                        "speaker", "AI",
                        "finalText", extractOutputText(event)
                ));
                case "response.output_text.delta" -> send(browserSession, Map.of(
                        "type", "text.out.partial",
                        "sessionId", token.sessionId(),
                        "turnNo", currentTurn(),
                        "text", event.path("delta").asText()
                ));
                case "response.output_text.done" -> send(browserSession, Map.of(
                        "type", "turn.complete",
                        "sessionId", token.sessionId(),
                        "turnNo", incrementTurn(),
                        "speaker", "AI",
                        "finalText", extractOutputText(event)
                ));
                case "conversation.item.input_audio_transcription.completed" -> send(browserSession, Map.of(
                        "type", "transcript.complete",
                        "sessionId", token.sessionId(),
                        "speaker", "USER",
                        "turnNo", currentTurn(),
                        "finalText", event.path("transcript").asText("")
                ));
                case "conversation.item.input_audio_transcription.delta" -> send(browserSession, Map.of(
                        "type", "transcript.partial",
                        "sessionId", token.sessionId(),
                        "speaker", "USER",
                        "turnNo", currentTurn(),
                        "text", event.path("delta").asText("")
                ));
                case "response.done" -> {
                    String finalText = extractResponseDoneText(event);
                    browserSession.getAttributes().put(UPSTREAM_RESPONSE_DONE_ATTRIBUTE, true);
                    send(browserSession, Map.of(
                            "type", "upstream.response.done",
                            "sessionId", token.sessionId(),
                            "status", event.path("response").path("status").asText(""),
                            "finalText", finalText
                    ));
                }
                case "error" -> send(browserSession, Map.of(
                        "type", "error",
                        "sessionId", token.sessionId(),
                        "code", event.path("error").path("code").asText("REALTIME_PROVIDER_ERROR"),
                        "message", event.path("error").path("message").asText("Realtime provider returned an error.")
                ));
                default -> {
                    // The relay intentionally ignores provider lifecycle events not needed by the browser contract.
                }
            }
        }

        private int currentTurn() {
            return turnCounter().get();
        }

        private int incrementTurn() {
            return turnCounter().getAndIncrement();
        }

        private void logUpstreamEvent(JsonNode event) {
            String type = event.path("type").asText();
            if ("response.output_audio.delta".equals(type)) {
                int chunkCount = ((AtomicInteger) browserSession.getAttributes()
                        .get(OUTPUT_AUDIO_CHUNK_COUNTER_ATTRIBUTE)).incrementAndGet();
                if (chunkCount == 1 || chunkCount % 25 == 0) {
                    log.info("Social voice upstream audio output. sessionId={}, chunkCount={}",
                            token.sessionId(), chunkCount);
                }
                return;
            }
            if ("error".equals(type)) {
                log.warn("Social voice upstream error event. sessionId={}, code={}, message={}",
                        token.sessionId(),
                        event.path("error").path("code").asText(""),
                        event.path("error").path("message").asText(""));
                return;
            }
            log.info("Social voice upstream event. sessionId={}, type={}", token.sessionId(), type);
        }

        private AtomicInteger turnCounter() {
            return (AtomicInteger) browserSession.getAttributes().get(TURN_COUNTER_ATTRIBUTE);
        }

        private String extractOutputText(JsonNode event) {
            List<String> candidates = new ArrayList<>();
            addTextCandidate(candidates, event.path("transcript").asText(""));
            addTextCandidate(candidates, event.path("text").asText(""));
            addTextCandidate(candidates, event.path("part").path("transcript").asText(""));
            addTextCandidate(candidates, event.path("part").path("text").asText(""));
            return String.join("\n", candidates).trim();
        }

        private String extractResponseDoneText(JsonNode event) {
            List<String> fragments = new ArrayList<>();
            JsonNode output = event.path("response").path("output");
            if (output.isArray()) {
                for (JsonNode item : output) {
                    addTextCandidate(fragments, item.path("text").asText(""));
                    addTextCandidate(fragments, item.path("transcript").asText(""));
                    JsonNode content = item.path("content");
                    if (content.isArray()) {
                        for (JsonNode part : content) {
                            addTextCandidate(fragments, part.path("text").asText(""));
                            addTextCandidate(fragments, part.path("transcript").asText(""));
                        }
                    }
                }
            }
            return String.join("\n", fragments).trim();
        }

        private void addTextCandidate(List<String> fragments, String candidate) {
            if (candidate != null && !candidate.isBlank()) {
                fragments.add(candidate);
            }
        }
    }
}
