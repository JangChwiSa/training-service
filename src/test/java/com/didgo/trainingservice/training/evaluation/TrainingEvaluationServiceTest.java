package com.didgo.trainingservice.training.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.didgo.trainingservice.external.openai.OpenAiAdapterException;
import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.didgo.trainingservice.external.openai.TrainingEvaluationAdapter;
import com.didgo.trainingservice.external.openai.TrainingEvaluationResultMapper;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationFeedback;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationLog;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationResult;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationStorageModel;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TrainingEvaluationServiceTest {

    RecordingAdapter adapter = new RecordingAdapter();
    TrainingEvaluationService service = new TrainingEvaluationService(
            adapter,
            new TrainingEvaluationResultMapper(),
            new OpenAiProperties(null, 50, "test", null, null)
    );

    @Test
    void usesOpenAiAdapterForSocialDialogueEvaluation() {
        TrainingEvaluationStorageModel result = service.evaluate(command(
                TrainingType.SOCIAL,
                "AI_EVALUATION",
                60,
                false
        ));

        assertThat(adapter.calls()).isEqualTo(1);
        assertThat(adapter.lastRequest.trainingType()).isEqualTo(TrainingType.SOCIAL);
        assertThat(adapter.lastRequest.metrics()).containsEntry("turnCount", 2);
        assertThat(adapter.lastRequest.metrics()).doesNotContainKey("userId");
        assertThat(result.score().score()).isEqualTo(88);
        assertThat(result.feedback().feedbackSource()).isEqualTo("AI");
        assertThat(result.score().rawMetricsJson()).contains("\"adapter\":\"test\"");
    }

    @Test
    void usesDeterministicScoringByDefaultForSafetyFocusAndDocument() {
        TrainingEvaluationStorageModel safety = service.evaluate(command(
                TrainingType.SAFETY,
                "CHOICE_RESULT",
                70,
                false
        ));
        TrainingEvaluationStorageModel focus = service.evaluate(command(
                TrainingType.FOCUS,
                "REACTION_PERFORMANCE",
                82,
                false
        ));
        TrainingEvaluationStorageModel document = service.evaluate(command(
                TrainingType.DOCUMENT,
                "ACCURACY_RATE",
                90,
                false
        ));

        assertThat(adapter.calls()).isZero();
        assertThat(safety.score().score()).isEqualTo(70);
        assertThat(focus.score().score()).isEqualTo(82);
        assertThat(document.score().score()).isEqualTo(90);
        assertThat(document.feedback().feedbackSource()).isEqualTo("SYSTEM");
        assertThat(document.score().rawMetricsJson()).contains("\"mode\":\"deterministic\"");
    }

    @Test
    void usesOpenAiAdapterForAdaptiveFeedbackWhenRequested() {
        TrainingEvaluationStorageModel result = service.evaluate(command(
                TrainingType.DOCUMENT,
                "ACCURACY_RATE",
                90,
                true
        ));

        assertThat(adapter.calls()).isEqualTo(1);
        assertThat(adapter.lastRequest.trainingType()).isEqualTo(TrainingType.DOCUMENT);
        assertThat(result.feedback().feedbackSource()).isEqualTo("AI");
        assertThat(result.score().score()).isEqualTo(88);
    }

    @Test
    void retriesOpenAiFailureAndReturnsFallbackStorageModel() {
        adapter.failure = new OpenAiAdapterException("temporary 5xx");

        TrainingEvaluationStorageModel result = service.evaluate(command(
                TrainingType.SOCIAL,
                "AI_EVALUATION",
                55,
                false
        ));

        assertThat(adapter.calls()).isEqualTo(2);
        assertThat(result.score().score()).isEqualTo(55);
        assertThat(result.feedback().feedbackSource()).isEqualTo("SYSTEM");
        assertThat(result.score().rawMetricsJson()).contains("\"fallback\":true");
        assertThat(result.score().rawMetricsJson()).contains("\"mode\":\"openai_failure\"");
        assertThat(result.score().rawMetricsJson()).contains("temporary 5xx");
    }

    @Test
    void timeoutReturnsFallbackStorageModel() {
        adapter.delayMs = 200;

        TrainingEvaluationStorageModel result = service.evaluate(command(
                TrainingType.SOCIAL,
                "AI_EVALUATION",
                58,
                false
        ));

        assertThat(adapter.calls()).isEqualTo(2);
        assertThat(result.score().score()).isEqualTo(58);
        assertThat(result.feedback().feedbackSource()).isEqualTo("SYSTEM");
        assertThat(result.score().rawMetricsJson()).contains("timed out");
    }

    private static TrainingEvaluationCommand command(
            TrainingType trainingType,
            String scoreType,
            int deterministicScore,
            boolean adaptiveFeedback
    ) {
        return new TrainingEvaluationCommand(
                trainingType,
                "?숇즺?먭쾶 ?꾩? ?붿껌?섍린",
                List.of(
                        new TrainingEvaluationLog("USER", "?꾩?二쇱떎 ???덈굹??"),
                        new TrainingEvaluationLog("AI", "?대뼡 遺遺꾩씠 ?대젮?곗떊媛??")
                ),
                Map.of("turnCount", 2),
                deterministicScore,
                scoreType,
                "?쒖뒪??湲곗??쇰줈 ?됯??덉뒿?덈떎.",
                "AI ?됯?瑜??ъ슜?????놁뼱 湲곕낯 ?됯? 湲곗????곸슜?덉뒿?덈떎.",
                adaptiveFeedback
        );
    }

    static class RecordingAdapter implements TrainingEvaluationAdapter {

        private final AtomicInteger calls = new AtomicInteger();
        private OpenAiAdapterException failure;
        private long delayMs;
        private TrainingEvaluationRequest lastRequest;

        @Override
        public TrainingEvaluationResult evaluate(TrainingEvaluationRequest request) {
            calls.incrementAndGet();
            lastRequest = request;
            if (delayMs > 0) {
                sleep(delayMs);
            }
            if (failure != null) {
                throw failure;
            }
            return new TrainingEvaluationResult(
                    88,
                    "AI_EVALUATION",
                    new TrainingEvaluationFeedback("AI ?붿빟", "AI ?곸꽭"),
                    "{\"adapter\":\"test\",\"fallback\":false}"
            );
        }

        int calls() {
            return calls.get();
        }

        private void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new OpenAiAdapterException("interrupted", exception);
            }
        }
    }
}
