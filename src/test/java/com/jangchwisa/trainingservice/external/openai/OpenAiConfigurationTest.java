package com.jangchwisa.trainingservice.external.openai;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jangchwisa.trainingservice.config.OpenAiConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OpenAiConfigurationTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withUserConfiguration(OpenAiConfig.class, LocalFakeOpenAiTrainingEvaluationAdapter.class,
                    OpenAiHttpTrainingEvaluationAdapter.class)
            .withPropertyValues(
                    "training.openai.timeout-ms=1500",
                    "training.openai.adapter=local-fake"
            );

    @Test
    void bindsTimeoutAndUsesLocalFakeAdapterByDefaultBoundary() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OpenAiProperties.class);
            assertThat(context).hasSingleBean(TrainingEvaluationAdapter.class);
            assertThat(context.getBean(OpenAiProperties.class).timeoutMs()).isEqualTo(1500);
            assertThat(context.getBean(TrainingEvaluationAdapter.class))
                    .isInstanceOf(LocalFakeOpenAiTrainingEvaluationAdapter.class);
        });
    }

    @Test
    void canSelectRealOpenAiHttpAdapter() {
        contextRunner
                .withPropertyValues(
                        "training.openai.adapter=openai",
                        "training.openai.api-key=test-key",
                        "training.openai.model=test-model"
                )
                .run(context -> assertThat(context.getBean(TrainingEvaluationAdapter.class))
                        .isInstanceOf(OpenAiHttpTrainingEvaluationAdapter.class));
    }
}
