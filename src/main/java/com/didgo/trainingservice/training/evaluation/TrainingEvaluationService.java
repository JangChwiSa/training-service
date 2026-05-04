package com.didgo.trainingservice.training.evaluation;

import com.didgo.trainingservice.external.openai.OpenAiAdapterException;
import com.didgo.trainingservice.external.openai.OpenAiProperties;
import com.didgo.trainingservice.external.openai.TrainingEvaluationAdapter;
import com.didgo.trainingservice.external.openai.TrainingEvaluationResultMapper;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationFeedbackModel;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationRequest;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationScoreModel;
import com.didgo.trainingservice.external.openai.dto.TrainingEvaluationStorageModel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.stereotype.Service;

@Service
public class TrainingEvaluationService {

    private static final int MAX_ATTEMPTS = 2;
    private static final String FEEDBACK_TYPE_SUMMARY = "SUMMARY";
    private static final String FEEDBACK_SOURCE_SYSTEM = "SYSTEM";

    private final TrainingEvaluationAdapter trainingEvaluationAdapter;
    private final TrainingEvaluationResultMapper resultMapper;
    private final OpenAiProperties properties;

    public TrainingEvaluationService(
            TrainingEvaluationAdapter trainingEvaluationAdapter,
            TrainingEvaluationResultMapper resultMapper,
            OpenAiProperties properties
    ) {
        this.trainingEvaluationAdapter = trainingEvaluationAdapter;
        this.resultMapper = resultMapper;
        this.properties = properties;
    }

    public TrainingEvaluationStorageModel evaluate(TrainingEvaluationCommand command) {
        if (!command.shouldUseAiEvaluation()) {
            return deterministicStorageModel(command, "deterministic");
        }

        TrainingEvaluationRequest request = new TrainingEvaluationRequest(
                command.trainingType(),
                command.scenarioTitle(),
                command.logs(),
                command.metrics()
        );

        OpenAiAdapterException lastException = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return resultMapper.toStorageModel(callWithTimeout(() -> trainingEvaluationAdapter.evaluate(request)));
            } catch (OpenAiAdapterException exception) {
                lastException = exception;
            }
        }

        String reason = lastException == null ? "unknown" : lastException.getMessage();
        return deterministicStorageModel(command, "openai_failure", reason);
    }

    private <T> T callWithTimeout(Callable<T> callable) {
        ExecutorService executor = Executors.newSingleThreadExecutor(new DaemonThreadFactory());
        Future<T> future = executor.submit(callable);
        try {
            return future.get(properties.timeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            throw new OpenAiAdapterException("OpenAI evaluation timed out.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new OpenAiAdapterException("OpenAI evaluation was interrupted.", exception);
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof OpenAiAdapterException openAiAdapterException) {
                throw openAiAdapterException;
            }
            throw new OpenAiAdapterException("OpenAI evaluation failed.", cause);
        } finally {
            executor.shutdownNow();
        }
    }

    private TrainingEvaluationStorageModel deterministicStorageModel(TrainingEvaluationCommand command, String mode) {
        return deterministicStorageModel(command, mode, null);
    }

    private TrainingEvaluationStorageModel deterministicStorageModel(
            TrainingEvaluationCommand command,
            String mode,
            String reason
    ) {
        return new TrainingEvaluationStorageModel(
                new TrainingEvaluationScoreModel(
                        command.deterministicScore(),
                        command.deterministicScoreType(),
                        fallbackRawMetricsJson(command, mode, reason)
                ),
                new TrainingEvaluationFeedbackModel(
                        FEEDBACK_TYPE_SUMMARY,
                        FEEDBACK_SOURCE_SYSTEM,
                        command.fallbackSummary(),
                        command.fallbackDetailText()
                )
        );
    }

    private String fallbackRawMetricsJson(TrainingEvaluationCommand command, String mode, String reason) {
        StringBuilder builder = new StringBuilder()
                .append("{\"fallback\":")
                .append(!"deterministic".equals(mode))
                .append(",\"mode\":\"")
                .append(mode)
                .append("\",\"trainingType\":\"")
                .append(command.trainingType().name())
                .append("\"");
        if (reason != null && !reason.isBlank()) {
            builder.append(",\"reason\":\"").append(escapeJson(reason)).append("\"");
        }
        builder.append("}");
        return builder.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "training-openai-evaluation");
            thread.setDaemon(true);
            return thread;
        }
    }
}
