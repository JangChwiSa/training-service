package com.didgo.trainingservice.external.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationFeedback;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "training.openai", name = "adapter", havingValue = "openai")
public class OpenAiHttpTrainingEvaluationAdapter implements TrainingEvaluationAdapter {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1/responses";
    private static final String SCORE_TYPE_AI_EVALUATION = "AI_EVALUATION";

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public OpenAiHttpTrainingEvaluationAdapter(OpenAiProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.timeoutMs()))
                .build());
    }

    OpenAiHttpTrainingEvaluationAdapter(OpenAiProperties properties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    @Override
    public TrainingEvaluationResult evaluate(TrainingEvaluationRequest request) {
        validateConfiguration();
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl()))
                    .timeout(Duration.ofMillis(properties.timeoutMs()))
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(request)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OpenAiAdapterException("OpenAI evaluation request failed with status " + response.statusCode() + ".");
            }
            return parseResponse(response.body());
        } catch (IOException exception) {
            throw new OpenAiAdapterException("OpenAI evaluation request failed.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OpenAiAdapterException("OpenAI evaluation request was interrupted.", exception);
        }
    }

    private void validateConfiguration() {
        if (!properties.hasApiKey()) {
            throw new OpenAiAdapterException("OpenAI API key is required when training.openai.adapter=openai.");
        }
        if (!properties.hasModel()) {
            throw new OpenAiAdapterException("OpenAI model is required when training.openai.adapter=openai.");
        }
    }

    private String requestBody(TrainingEvaluationRequest request) throws IOException {
        JsonNode body = objectMapper.valueToTree(new OpenAiEvaluationRequestBody(
                properties.model(),
                instructions(),
                userInput(request),
                new TextFormat(new JsonSchemaFormat(
                        "json_schema",
                        "training_evaluation",
                        true,
                        objectMapper.readTree("""
                                {
                                  "type": "object",
                                  "additionalProperties": false,
                                  "properties": {
                                    "score": { "type": "integer", "minimum": 0, "maximum": 100 },
                                    "summary": { "type": "string" },
                                    "detailText": { "type": "string" },
                                    "rawMetricsJson": { "type": "string" }
                                  },
                                  "required": ["score", "summary", "detailText", "rawMetricsJson"]
                                }
                                """)
                ))
        ));
        return objectMapper.writeValueAsString(body);
    }

    private String instructions() {
        return """
                Evaluate a completed training session. Return only structured JSON.
                Score must be an integer from 0 to 100.
                Feedback must be concise and based only on the provided training logs and metrics.
                Do not infer or include user identity.
                """;
    }

    private String userInput(TrainingEvaluationRequest request) throws IOException {
        return objectMapper.writeValueAsString(new EvaluationInput(
                request.trainingType().name(),
                request.scenarioTitle(),
                request.logs(),
                request.metrics()
        ));
    }

    private TrainingEvaluationResult parseResponse(String responseBody) throws IOException {
        JsonNode response = objectMapper.readTree(responseBody);
        String outputText = response.path("output_text").asText(null);
        if (outputText == null || outputText.isBlank()) {
            outputText = extractOutputText(response);
        }
        if (outputText == null || outputText.isBlank()) {
            throw new OpenAiAdapterException("OpenAI response did not contain output text.");
        }

        JsonNode evaluation = objectMapper.readTree(outputText);
        int score = evaluation.path("score").asInt(-1);
        String summary = evaluation.path("summary").asText(null);
        String detailText = evaluation.path("detailText").asText(null);
        String rawMetricsJson = evaluation.path("rawMetricsJson").asText(null);
        return new TrainingEvaluationResult(
                score,
                SCORE_TYPE_AI_EVALUATION,
                new TrainingEvaluationFeedback(summary, detailText),
                rawMetricsJson
        );
    }

    private String extractOutputText(JsonNode response) {
        for (JsonNode outputItem : response.path("output")) {
            for (JsonNode contentItem : outputItem.path("content")) {
                if ("output_text".equals(contentItem.path("type").asText())) {
                    return contentItem.path("text").asText(null);
                }
            }
        }
        return null;
    }

    private String baseUrl() {
        return properties.baseUrl() == null ? DEFAULT_BASE_URL : properties.baseUrl();
    }

    private record OpenAiEvaluationRequestBody(String model, String instructions, String input, TextFormat text) {
    }

    private record TextFormat(JsonSchemaFormat format) {
    }

    private record JsonSchemaFormat(String type, String name, boolean strict, JsonNode schema) {
    }

    private record EvaluationInput(
            String trainingType,
            String scenarioTitle,
            java.util.List<TrainingEvaluationLog> logs,
            java.util.Map<String, Object> metrics
    ) {
    }
}
