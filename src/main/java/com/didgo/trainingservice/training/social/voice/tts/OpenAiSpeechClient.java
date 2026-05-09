package com.didgo.trainingservice.training.social.voice.tts;

import com.didgo.trainingservice.external.openai.OpenAiAdapterException;
import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpenAiSpeechClient {

    private final OpenAiProperties openAiProperties;
    private final OpenAiSpeechProperties speechProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public OpenAiSpeechClient(
            OpenAiProperties openAiProperties,
            OpenAiSpeechProperties speechProperties,
            ObjectMapper objectMapper
    ) {
        this(openAiProperties, speechProperties, objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(openAiProperties.timeoutMs()))
                .build());
    }

    OpenAiSpeechClient(
            OpenAiProperties openAiProperties,
            OpenAiSpeechProperties speechProperties,
            ObjectMapper objectMapper,
            HttpClient httpClient
    ) {
        this.openAiProperties = openAiProperties;
        this.speechProperties = speechProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public byte[] createSpeech(String input) {
        if (!openAiProperties.hasApiKey()) {
            throw new OpenAiAdapterException("OPENAI_API_KEY is required for opening speech generation.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(speechProperties.url()))
                    .timeout(Duration.ofMillis(openAiProperties.timeoutMs()))
                    .header("Authorization", "Bearer " + openAiProperties.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(new SpeechRequest(
                            speechProperties.model(),
                            speechProperties.voice(),
                            input,
                            speechProperties.instructions(),
                            speechProperties.responseFormat()
                    ))))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OpenAiAdapterException("OpenAI speech request failed with status " + response.statusCode() + ".");
            }
            return response.body();
        } catch (IOException exception) {
            throw new OpenAiAdapterException("OpenAI speech request failed.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OpenAiAdapterException("OpenAI speech request was interrupted.", exception);
        }
    }

    private record SpeechRequest(
            String model,
            String voice,
            String input,
            String instructions,
            String response_format
    ) {
    }
}
