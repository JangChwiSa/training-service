package com.jangchwisa.trainingservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SwaggerOpenApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerUiIsAvailable() throws Exception {
        HttpResponse<String> response = get("/swagger-ui.html");

        assertThat(response.statusCode()).isIn(200, 302);
        assertThat(response.headers().firstValue("location").orElse("/swagger-ui/index.html"))
                .contains("swagger-ui");
    }

    @Test
    void externalTrainingOpenApiGroupContainsOnlyExternalTrainingApis() throws Exception {
        JsonNode body = objectMapper.readTree(get("/v3/api-docs/external-training").body());

        assertThat(body.path("info").path("title").asText()).isEqualTo("Training Service API");
        assertThat(body.path("components").path("securitySchemes").has("TrustedUserHeader")).isTrue();
        assertThat(body.path("paths")
                .path("/api/trainings/progress")
                .path("get")
                .path("parameters"))
                .allSatisfy(parameter -> assertThat(parameter.path("name").asText()).isNotEqualTo("currentUser"));
        assertThat(body.path("paths").has("/api/trainings/progress")).isTrue();
        assertThat(body.path("paths").has("/internal/trainings/users/{userId}/summary")).isFalse();
    }

    @Test
    void internalTrainingOpenApiGroupContainsOnlyInternalTrainingApis() throws Exception {
        JsonNode body = objectMapper.readTree(get("/v3/api-docs/internal-training").body());

        assertThat(body.path("paths").has("/internal/trainings/users/{userId}/summary")).isTrue();
        assertThat(body.path("paths").has("/api/trainings/progress")).isFalse();
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }
}
