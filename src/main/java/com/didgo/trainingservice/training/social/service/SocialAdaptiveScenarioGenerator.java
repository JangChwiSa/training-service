package com.didgo.trainingservice.training.social.service;

import com.didgo.trainingservice.external.openai.OpenAiAdapterException;
import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.didgo.trainingservice.training.social.entity.SocialJobType;
import com.didgo.trainingservice.training.social.repository.SocialTrainingRepository.SocialHistoryRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SocialAdaptiveScenarioGenerator {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1/responses";
    private static final Pattern QUOTED_SPEECH_PATTERN = Pattern.compile("[\"“”'‘’]([^\"“”'‘’]{2,120})[\"“”'‘’]");

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public SocialAdaptiveScenarioGenerator(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.timeoutMs()))
                .build();
    }

    public SocialAdaptiveScenarioDraft generate(SocialJobType jobType, List<SocialHistoryRow> historyRows) {
        if (!"openai".equalsIgnoreCase(properties.adapter()) || !properties.hasApiKey() || !properties.hasModel()) {
            return fallbackDraft(jobType, historyRows);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl()))
                    .timeout(Duration.ofMillis(properties.timeoutMs()))
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(jobType, historyRows)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new OpenAiAdapterException("OpenAI adaptive scenario request failed with status " + response.statusCode() + ".");
            }
            return parseResponse(response.body(), jobType, historyRows);
        } catch (IOException exception) {
            return fallbackDraft(jobType, historyRows);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return fallbackDraft(jobType, historyRows);
        } catch (OpenAiAdapterException exception) {
            return fallbackDraft(jobType, historyRows);
        }
    }

    private String requestBody(SocialJobType jobType, List<SocialHistoryRow> historyRows) throws IOException {
        JsonNode body = objectMapper.valueToTree(new OpenAiScenarioRequestBody(
                properties.model(),
                instructions(),
                objectMapper.writeValueAsString(new ScenarioInput(jobType.name(), historyRows)),
                new TextFormat(new JsonSchemaFormat(
                        "json_schema",
                        "social_adaptive_scenario",
                        true,
                        objectMapper.readTree("""
                                {
                                  "type": "object",
                                  "additionalProperties": false,
                                  "properties": {
                                    "title": { "type": "string" },
                                    "backgroundText": { "type": "string" },
                                    "situationText": { "type": "string" },
                                    "characterInfo": { "type": "string" },
                                    "difficulty": { "type": "string", "enum": ["1", "2", "3"] },
                                    "categoryCode": { "type": "string" },
                                    "evaluationPoint": { "type": "string" },
                                    "exampleAnswer": { "type": "string" },
                                    "focusSummary": { "type": "string" }
                                  },
                                  "required": [
                                    "title",
                                    "backgroundText",
                                    "situationText",
                                    "characterInfo",
                                    "difficulty",
                                    "categoryCode",
                                    "evaluationPoint",
                                    "exampleAnswer",
                                    "focusSummary"
                                  ]
                                }
                                """)
                ))
        ));
        return objectMapper.writeValueAsString(body);
    }

    private String instructions() {
        return """
                Create one personalized Korean workplace social communication training scenario.
                Match the existing scenario style exactly.

                situationText format:
                - A short third-person setup.
                - The manager/senior/coworker gives one short, vague instruction or request in quotes.
                - Then explain what is unclear outside the quotes.
                - End with "어떻게 대답할까요?"

                Good examples:
                - 사무실에서 선임이 "이 자료 좀 준비해 주세요"라고 말했습니다. 필요한 수량과 마감 시간이 분명하지 않습니다. 어떻게 대답할까요?
                - 작업 중 선임이 "이거 미리 정리해 주세요"라고 말했습니다. 무엇을 어디까지 정리해야 하는지 분명하지 않습니다. 어떻게 대답할까요?

                Strict rules:
                - The quoted text must be the counterpart's first instruction/request, not the learner's answer.
                - The quoted text must be intentionally incomplete and short.
                - Do not include deadline, quantity, format, scope, or detailed requirements inside the quoted text.
                - Put missing details after the quote, not inside it.
                - Bad: "다음 주까지 이 자료를 정리해 주세요" because the quote already includes a deadline.
                - Bad: "A4 한 장으로 정리해 주세요" because the quote already includes a format/amount.
                - Good: "이 자료 좀 정리해 주세요" and then explain that deadline, amount, and format are unclear.
                - Do not put learner responses like "확인하고 싶어요", "알려주실 수 있을까요", "어떻게 하면 될까요" in quotes.
                - Keep situationText realistic, concrete, and short.
                - Do not include personally identifying information.
                - Return only structured JSON.
                """;
    }

    private SocialAdaptiveScenarioDraft parseResponse(
            String responseBody,
            SocialJobType jobType,
            List<SocialHistoryRow> historyRows
    ) throws IOException {
        JsonNode response = objectMapper.readTree(responseBody);
        String outputText = response.path("output_text").asText(null);
        if (outputText == null || outputText.isBlank()) {
            outputText = extractOutputText(response);
        }
        if (outputText == null || outputText.isBlank()) {
            return fallbackDraft(jobType, historyRows);
        }

        JsonNode scenario = objectMapper.readTree(outputText);
        SocialAdaptiveScenarioDraft draft = new SocialAdaptiveScenarioDraft(
                textOrDefault(scenario, "title", "취약한 부분 맞춤 연습"),
                textOrDefault(scenario, "backgroundText", "최근 훈련 기록을 바탕으로 다시 연습할 상황입니다."),
                textOrDefault(scenario, "situationText", fallbackSituation(jobType)),
                textOrDefault(scenario, "characterInfo", "상대는 직장 동료입니다. 사용자는 필요한 정보를 정중하게 확인해야 합니다."),
                textOrDefault(scenario, "difficulty", "2"),
                textOrDefault(scenario, "categoryCode", "ADAPTIVE_PRACTICE"),
                textOrDefault(scenario, "evaluationPoint", "상대의 요청을 확인하고 필요한 정보를 정중하게 물어보는가?"),
                textOrDefault(scenario, "exampleAnswer", "제가 정확히 처리할 수 있도록 필요한 내용을 한 번만 더 알려주실 수 있을까요?"),
                textOrDefault(scenario, "focusSummary", fallbackFocusSummary(historyRows))
        );
        return hasValidCounterpartOpening(draft.situationText()) ? draft : fallbackDraft(jobType, historyRows);
    }

    private boolean hasValidCounterpartOpening(String situationText) {
        String opening = quotedOpening(situationText);
        if (opening.isBlank()) {
            return false;
        }
        String normalized = opening.replaceAll("\\s+", "");
        if (normalized.contains("확인하고싶") || normalized.contains("알려주실수") || normalized.contains("될까요")
                || normalized.contains("할까요") || normalized.contains("싶어요") || normalized.contains("싶습니다")) {
            return false;
        }
        if (containsOverSpecifiedDetail(normalized)) {
            return false;
        }
        return normalized.contains("주세요") || normalized.contains("해요") || normalized.contains("돼요")
                || normalized.contains("처리") || normalized.contains("준비") || normalized.contains("정리")
                || normalized.contains("옮겨") || normalized.contains("확인");
    }

    private boolean containsOverSpecifiedDetail(String normalizedOpening) {
        return normalizedOpening.contains("까지")
                || normalizedOpening.contains("다음주")
                || normalizedOpening.contains("이번주")
                || normalizedOpening.contains("오늘")
                || normalizedOpening.contains("내일")
                || normalizedOpening.contains("오전")
                || normalizedOpening.contains("오후")
                || normalizedOpening.contains("마감")
                || normalizedOpening.contains("분량")
                || normalizedOpening.contains("형식")
                || normalizedOpening.contains("수량")
                || normalizedOpening.contains("몇부")
                || normalizedOpening.matches(".*\\d+\\s*(시|분|일|월|개|부|장).*");
    }

    private String quotedOpening(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        Matcher matcher = QUOTED_SPEECH_PATTERN.matcher(text);
        String result = "";
        while (matcher.find()) {
            result = matcher.group(1).trim();
        }
        return result;
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

    private String textOrDefault(JsonNode node, String fieldName, String fallback) {
        String value = node.path(fieldName).asText(null);
        return value == null || value.isBlank() ? fallback : sanitizeGeneratedText(value);
    }

    private String sanitizeGeneratedText(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replaceAll("\\s+", " ")
                .replaceFirst("^[-*]\\s*", "")
                .trim();
    }

    private SocialAdaptiveScenarioDraft fallbackDraft(SocialJobType jobType, List<SocialHistoryRow> historyRows) {
        String focusSummary = fallbackFocusSummary(historyRows);
        FallbackScenario fallbackScenario = fallbackScenario(jobType);
        return new SocialAdaptiveScenarioDraft(
                fallbackScenario.title(),
                focusSummary,
                fallbackScenario.situationText(),
                fallbackScenario.characterInfo(),
                "2",
                "ADAPTIVE_PRACTICE",
                fallbackScenario.evaluationPoint(),
                fallbackScenario.exampleAnswer(),
                focusSummary
        );
    }

    private String fallbackSituation(SocialJobType jobType) {
        return fallbackScenario(jobType).situationText();
    }

    private FallbackScenario fallbackScenario(SocialJobType jobType) {
        List<FallbackScenario> scenarios = jobType == SocialJobType.LABOR ? laborFallbackScenarios() : officeFallbackScenarios();
        return scenarios.get(ThreadLocalRandom.current().nextInt(scenarios.size()));
    }

    private List<FallbackScenario> officeFallbackScenarios() {
        return List.of(
                new FallbackScenario(
                        "자료 준비 범위 확인하기",
                        "사무실에서 선임이 \"이 자료 좀 준비해 주세요\"라고 말했습니다. 필요한 수량과 마감 시간이 분명하지 않습니다. 어떻게 대답할까요?",
                        "상대는 업무를 맡긴 선임입니다. 사용자는 필요한 정보를 정중하게 확인해야 합니다.",
                        "수량과 마감 시간을 구체적으로 확인했는가?",
                        "네, 정확히 준비하겠습니다. 몇 부가 필요하고 언제까지 준비하면 될까요?"
                ),
                new FallbackScenario(
                        "우선순위 물어보기",
                        "동시에 하던 일이 있는데 선임이 \"이건 먼저 처리해 주세요\"라고 말했습니다. 어떤 일을 먼저 해야 할지 헷갈립니다. 어떻게 대답할까요?",
                        "상대는 바쁜 선임입니다. 사용자는 현재 하던 일과 새 요청의 우선순위를 확인해야 합니다.",
                        "현재 업무 상황을 말하고 우선순위를 정중하게 물었는가?",
                        "지금 맡은 일을 진행 중인데, 이 요청을 먼저 처리하면 될까요? 우선순위를 확인하고 바로 하겠습니다."
                ),
                new FallbackScenario(
                        "모르는 업무 다시 묻기",
                        "처음 해보는 문서 정리를 맡았는데 선임이 \"전에 말한 방식대로 정리하면 돼요\"라고 말했습니다. 방식이 정확히 기억나지 않습니다. 어떻게 대답할까요?",
                        "상대는 이미 설명했다고 생각합니다. 사용자는 모르는 부분을 숨기지 않고 다시 확인해야 합니다.",
                        "기억나지 않는 부분을 솔직히 말하고 다시 설명을 요청했는가?",
                        "죄송합니다. 제가 정확히 기억하지 못했습니다. 실수하지 않도록 정리 방식만 한 번 더 알려주실 수 있을까요?"
                )
        );
    }

    private List<FallbackScenario> laborFallbackScenarios() {
        return List.of(
                new FallbackScenario(
                        "작업 범위 확인하기",
                        "작업 중 선임이 \"이거 미리 정리해 주세요\"라고 말했습니다. 무엇을 어디까지 정리해야 하는지 분명하지 않습니다. 어떻게 대답할까요?",
                        "상대는 현장 선임입니다. 사용자는 작업 범위를 구체적으로 확인해야 합니다.",
                        "정리할 대상과 범위를 구체적으로 물었는가?",
                        "네, 바로 하겠습니다. 어떤 물건을 어디까지 정리하면 되는지 한 번만 알려주실 수 있을까요?"
                ),
                new FallbackScenario(
                        "안전하게 도움 요청하기",
                        "무거운 박스를 옮기라는 말을 들었는데 혼자 들기에는 위험해 보입니다. 선임은 \"이 박스 옮겨 주세요\"라고 말했습니다. 어떻게 대답할까요?",
                        "상대는 작업 속도를 중요하게 생각합니다. 사용자는 안전을 지키면서 도움을 요청해야 합니다.",
                        "위험한 이유를 말하고 도움을 요청했는가?",
                        "혼자 들면 다칠 수 있을 것 같습니다. 안전하게 옮기기 위해 같이 들어주실 수 있을까요?"
                ),
                new FallbackScenario(
                        "작업 순서 다시 확인하기",
                        "처음 하는 기계 작업을 맡았는데 순서가 헷갈립니다. 선임은 \"아까 말한 대로 하면 돼요\"라고 말했습니다. 어떻게 대답할까요?",
                        "상대는 이미 설명했다고 생각합니다. 사용자는 안전을 위해 작업 순서를 다시 확인해야 합니다.",
                        "안전한 이유로 작업 순서를 다시 요청했는가?",
                        "죄송합니다. 안전하게 작업하려고 순서를 다시 확인하고 싶습니다. 처음부터 한 번만 더 알려주실 수 있을까요?"
                )
        );
    }

    private String fallbackFocusSummary(List<SocialHistoryRow> historyRows) {
        if (historyRows == null || historyRows.isEmpty()) {
            return "아직 충분한 사회성 훈련 기록이 없어 기본 약점인 '모호한 지시 확인하기'를 연습합니다.";
        }
        SocialHistoryRow lowest = historyRows.stream()
                .filter(row -> row.score() != null)
                .min((left, right) -> Integer.compare(left.score(), right.score()))
                .orElse(historyRows.get(0));
        return "최근 기록 중 '" + safe(lowest.scenarioTitle()) + "'에서 보완이 필요했던 내용을 바탕으로 연습합니다.";
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "사회성 훈련" : value.trim();
    }

    private String baseUrl() {
        return properties.baseUrl() == null ? DEFAULT_BASE_URL : properties.baseUrl();
    }

    private record OpenAiScenarioRequestBody(String model, String instructions, String input, TextFormat text) {
    }

    private record TextFormat(JsonSchemaFormat format) {
    }

    private record JsonSchemaFormat(String type, String name, boolean strict, JsonNode schema) {
    }

    private record ScenarioInput(String jobType, List<SocialHistoryRow> recentHistory) {
    }

    private record FallbackScenario(
            String title,
            String situationText,
            String characterInfo,
            String evaluationPoint,
            String exampleAnswer
    ) {
    }
}
