package com.jangchwisa.trainingservice.config;

import com.jangchwisa.trainingservice.common.security.AuthenticatedUser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerOpenApiConfig {

    private static final String TRUSTED_USER_HEADER_SCHEME = "TrustedUserHeader";

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthenticatedUser.class);
    }

    @Bean
    public OpenAPI trainingServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Training Service API")
                        .version("v1")
                        .description("""
                                Training Service owns training content, sessions, progress, scores, feedback, summaries, and TrainingCompleted event publishing.
                                External clients must not send userId in request bodies or query parameters. User identity is supplied by the API Gateway through the trusted X-User-Id header.
                                """)
                        .license(new License().name("Internal")))
                .components(new Components()
                        .addSecuritySchemes(TRUSTED_USER_HEADER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-User-Id")
                                .description("Trusted user id header set by API Gateway after token validation.")))
                .addSecurityItem(new SecurityRequirement().addList(TRUSTED_USER_HEADER_SCHEME));
    }

    @Bean
    public GroupedOpenApi externalTrainingOpenApi() {
        return GroupedOpenApi.builder()
                .group("external-training")
                .displayName("External Training APIs")
                .pathsToMatch("/api/trainings/**")
                .build();
    }

    @Bean
    public GroupedOpenApi internalTrainingOpenApi() {
        return GroupedOpenApi.builder()
                .group("internal-training")
                .displayName("Internal Training APIs")
                .pathsToMatch("/internal/trainings/**")
                .build();
    }
}
