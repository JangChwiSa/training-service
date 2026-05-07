package com.didgo.trainingservice.training.social.voice.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.didgo.trainingservice.training.social.voice.SocialVoiceSessionToken;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

class OpenAiRealtimeUpstreamClientFactoryLiveTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void connectsToProductionRealtimeApiWhenApiKeyIsAvailable() throws Exception {
        OpenAiRealtimeProperties realtimeProperties = new OpenAiRealtimeProperties(
                env("OPENAI_REALTIME_URL"),
                env("OPENAI_REALTIME_MODEL"),
                env("OPENAI_REALTIME_VOICE"),
                env("OPENAI_REALTIME_INPUT_AUDIO_FORMAT"),
                parseRate(env("OPENAI_REALTIME_INPUT_AUDIO_RATE")),
                env("OPENAI_REALTIME_OUTPUT_AUDIO_FORMAT")
        );
        OpenAiRealtimeUpstreamClientFactory factory = new OpenAiRealtimeUpstreamClientFactory(
                new OpenAiProperties(System.getenv("OPENAI_API_KEY"), 10_000, "openai", null, null),
                realtimeProperties
        );
        CountDownLatch providerEvent = new CountDownLatch(1);
        AtomicReference<String> firstEvent = new AtomicReference<>();

        RealtimeUpstreamClient client = factory.connect(token(), new RealtimeUpstreamEventHandler() {
            @Override
            public void onEvent(String eventJson) {
                firstEvent.compareAndSet(null, eventJson);
                providerEvent.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                firstEvent.compareAndSet(null, "{\"type\":\"error\",\"message\":\"" + throwable.getMessage() + "\"}");
                providerEvent.countDown();
            }

            @Override
            public void onClose() {
            }
        });

        try {
            client.send(sessionUpdate(realtimeProperties));

            assertThat(providerEvent.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(firstEvent.get()).contains("\"type\"");
            assertThat(firstEvent.get()).doesNotContain("\"type\":\"error\"");
        } finally {
            client.close();
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void completesOneRealtimeTextRoundTripWhenApiKeyIsAvailable() throws Exception {
        OpenAiRealtimeProperties realtimeProperties = new OpenAiRealtimeProperties(
                env("OPENAI_REALTIME_URL"),
                env("OPENAI_REALTIME_MODEL"),
                env("OPENAI_REALTIME_VOICE"),
                env("OPENAI_REALTIME_INPUT_AUDIO_FORMAT"),
                parseRate(env("OPENAI_REALTIME_INPUT_AUDIO_RATE")),
                env("OPENAI_REALTIME_OUTPUT_AUDIO_FORMAT")
        );
        OpenAiRealtimeUpstreamClientFactory factory = new OpenAiRealtimeUpstreamClientFactory(
                new OpenAiProperties(System.getenv("OPENAI_API_KEY"), 10_000, "openai", null, null),
                realtimeProperties
        );
        CountDownLatch responseDone = new CountDownLatch(1);
        AtomicReference<String> finalResponse = new AtomicReference<>();
        List<String> eventTypes = new ArrayList<>();

        RealtimeUpstreamClient client = factory.connect(token(), new RealtimeUpstreamEventHandler() {
            @Override
            public void onEvent(String eventJson) {
                if (eventJson.contains("\"type\":\"")) {
                    eventTypes.add(eventJson);
                }
                if (eventJson.contains("\"type\":\"response.done\"")) {
                    finalResponse.set(eventJson);
                    responseDone.countDown();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                finalResponse.set("{\"type\":\"error\",\"message\":\"" + throwable.getMessage() + "\"}");
                responseDone.countDown();
            }

            @Override
            public void onClose() {
            }
        });

        try {
            client.send(sessionUpdate(realtimeProperties));
            client.send("""
                    {"type":"conversation.item.create","item":{"type":"message","role":"user","content":[{"type":"input_text","text":"복사 수량을 10부로 해달라고 한국어로 한 문장만 답하세요."}]}}
                    """);
            client.send("""
                    {"type":"response.create","response":{"output_modalities":["text"]}}
                    """);

            assertThat(responseDone.await(20, TimeUnit.SECONDS)).isTrue();
            assertThat(finalResponse.get()).contains("\"type\":\"response.done\"");
            assertThat(finalResponse.get()).doesNotContain("\"type\":\"error\"");
            assertThat(finalResponse.get()).contains("10");
        } finally {
            client.close();
        }
    }

    private static SocialVoiceSessionToken token() {
        return new SocialVoiceSessionToken(
                "live-test-token",
                1L,
                1L,
                1L,
                "회의 자료 복사 수량이 모호한 상황",
                "안녕하세요. 같이 연습해볼게요.",
                "/static/opening/social/1/live-test.pcm",
                Instant.now().plusSeconds(60)
        );
    }

    private static String sessionUpdate(OpenAiRealtimeProperties properties) {
        return """
                {"type":"session.update","session":{"type":"realtime","model":"%s","output_modalities":["audio"],"audio":{"input":{"format":{"type":"%s","rate":%d},"turn_detection":null},"output":{"format":{"type":"%s","rate":%d},"voice":"%s"}},"instructions":"You are roleplaying as the other person in a Korean workplace training scenario. Stay fully in character and respond as if the situation is happening right now."}}
                """.formatted(
                properties.model(),
                properties.inputAudioFormat(),
                properties.inputAudioRate(),
                properties.outputAudioFormat(),
                properties.inputAudioRate(),
                properties.voice()
        );
    }

    private static String env(String name) {
        return System.getenv(name);
    }

    private static int parseRate(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }
}
