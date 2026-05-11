package com.didgo.trainingservice.training.focus.service;

import com.didgo.trainingservice.common.exception.ErrorCode;
import com.didgo.trainingservice.common.exception.TrainingServiceException;
import com.didgo.trainingservice.common.security.CurrentUser;
import com.didgo.trainingservice.training.completion.TrainingCompletionCommand;
import com.didgo.trainingservice.training.completion.TrainingCompletionFeedback;
import com.didgo.trainingservice.training.completion.TrainingCompletionProgress;
import com.didgo.trainingservice.training.completion.TrainingCompletionResult;
import com.didgo.trainingservice.training.completion.TrainingCompletionScore;
import com.didgo.trainingservice.training.completion.TrainingCompletionService;
import com.didgo.trainingservice.training.completion.TrainingCompletionSummary;
import com.didgo.trainingservice.training.focus.dto.CompleteFocusSessionRequest;
import com.didgo.trainingservice.training.focus.dto.CompleteFocusSessionResponse;
import com.didgo.trainingservice.training.focus.dto.FocusCommandResponse;
import com.didgo.trainingservice.training.focus.dto.FocusProgressResponse;
import com.didgo.trainingservice.training.focus.dto.StartFocusSessionResponse;
import com.didgo.trainingservice.training.focus.repository.FocusTrainingRepository;
import com.didgo.trainingservice.training.focus.repository.FocusTrainingRepository.FocusCommandRow;
import com.didgo.trainingservice.training.focus.repository.FocusTrainingRepository.FocusLevelRuleRow;
import com.didgo.trainingservice.training.focus.repository.FocusTrainingRepository.NewFocusCommand;
import com.didgo.trainingservice.training.focus.repository.FocusTrainingRepository.ScoredFocusReaction;
import com.didgo.trainingservice.training.session.entity.TrainingSession;
import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.didgo.trainingservice.training.session.service.SessionOwnershipValidator;
import com.didgo.trainingservice.training.session.service.TrainingSessionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FocusTrainingService {

    private final FocusTrainingRepository focusTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final FocusCommandGenerator focusCommandGenerator;
    private final SessionOwnershipValidator sessionOwnershipValidator;
    private final TrainingCompletionService trainingCompletionService;

    @Autowired
    public FocusTrainingService(
            FocusTrainingRepository focusTrainingRepository,
            TrainingSessionService trainingSessionService,
            FocusCommandGenerator focusCommandGenerator,
            SessionOwnershipValidator sessionOwnershipValidator,
            TrainingCompletionService trainingCompletionService
    ) {
        this.focusTrainingRepository = focusTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.focusCommandGenerator = focusCommandGenerator;
        this.sessionOwnershipValidator = sessionOwnershipValidator;
        this.trainingCompletionService = trainingCompletionService;
    }

    public FocusTrainingService(
            FocusTrainingRepository focusTrainingRepository,
            TrainingSessionService trainingSessionService,
            FocusCommandGenerator focusCommandGenerator
    ) {
        this(focusTrainingRepository, trainingSessionService, focusCommandGenerator, null, null);
    }

    @Transactional(readOnly = true)
    public FocusProgressResponse getProgress(long userId) {
        return focusTrainingRepository.findProgress(userId)
                .orElseGet(() -> new FocusProgressResponse(1, 1, null, null, null));
    }

    @Transactional
    public StartFocusSessionResponse startSession(CurrentUser currentUser, int level) {
        FocusLevelRuleRow levelRule = focusTrainingRepository.findActiveLevelRule(level)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Focus level rule was not found."));
        FocusProgressResponse progress = getProgress(currentUser.userId());
        if (level > progress.highestUnlockedLevel()) {
            throw new TrainingServiceException(ErrorCode.FORBIDDEN, "Focus level is locked.");
        }

        TrainingSession session = trainingSessionService.createSession(
                currentUser,
                new CreateTrainingSessionCommand(currentUser.userId(), TrainingType.FOCUS, String.valueOf(level), null)
        );
        List<NewFocusCommand> commands = focusCommandGenerator.generate(
                levelRule.durationSeconds(),
                levelRule.commandIntervalMs(),
                levelRule.commandComplexity()
        );
        List<FocusCommandResponse> savedCommands = focusTrainingRepository.saveCommands(session.sessionId(), commands);

        return new StartFocusSessionResponse(session.sessionId(), level, levelRule.durationSeconds(), savedCommands);
    }

    public CompleteFocusSessionResponse completeSession(
            CurrentUser currentUser,
            long sessionId,
            CompleteFocusSessionRequest request
    ) {
        ensureCompletionDependencies();
        sessionOwnershipValidator.validateOwner(sessionId, currentUser);
        int level = focusTrainingRepository.findSessionLevel(sessionId)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Focus training session was not found."));
        FocusLevelRuleRow levelRule = focusTrainingRepository.findActiveLevelRule(level)
                .orElseThrow(() -> new TrainingServiceException(ErrorCode.NOT_FOUND, "Focus level rule was not found."));
        List<FocusCommandRow> commands = focusTrainingRepository.findCommands(sessionId);
        if (commands.isEmpty()) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Focus completion requires commands.");
        }

        Map<Long, FocusCommandRow> commandById = new HashMap<>();
        for (FocusCommandRow command : commands) {
            commandById.put(command.commandId(), command);
        }
        List<ScoredFocusReaction> reactions = request.reactions().stream()
                .map(reaction -> {
                    FocusCommandRow command = commandById.get(reaction.commandId());
                    if (command == null) {
                        throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Unknown focus command id.");
                    }
                    return ScoredFocusReaction.from(reaction, command.expectedAction().equalsIgnoreCase(reaction.userInput().trim()));
                })
                .toList();
        if (reactions.size() != commands.size() || reactions.stream().map(ScoredFocusReaction::commandId).distinct().count() != reactions.size()) {
            throw new TrainingServiceException(ErrorCode.VALIDATION_ERROR, "Focus completion requires one reaction per command.");
        }

        int correctCount = (int) reactions.stream().filter(ScoredFocusReaction::correct).count();
        int totalCount = reactions.size();
        int wrongCount = totalCount - correctCount;
        BigDecimal accuracyRate = BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
        int averageReactionMs = (int) Math.round(reactions.stream()
                .mapToInt(ScoredFocusReaction::reactionMs)
                .average()
                .orElse(0));
        int score = accuracyRate.setScale(0, RoundingMode.HALF_UP).intValue();
        boolean unlockedNextLevel = accuracyRate.compareTo(levelRule.requiredAccuracyRate()) >= 0;
        FocusProgressResponse previousProgress = getProgress(currentUser.userId());
        int highestUnlockedLevel = unlockedNextLevel
                ? Math.max(previousProgress.highestUnlockedLevel(), level + 1)
                : Math.max(previousProgress.highestUnlockedLevel(), level);
        int currentLevel = Math.max(previousProgress.currentLevel(), highestUnlockedLevel);

        TrainingCompletionResult result = trainingCompletionService.complete(new TrainingCompletionCommand(
                currentUser.userId(),
                sessionId,
                TrainingType.FOCUS,
                new TrainingCompletionScore(
                        score,
                        "REACTION_PERFORMANCE",
                        correctCount,
                        totalCount,
                        accuracyRate,
                        wrongCount,
                        averageReactionMs,
                        null
                ),
                new TrainingCompletionFeedback(
                        "SUMMARY",
                        "SYSTEM",
                        "집중력 훈련이 완료되었습니다.",
                        "명령에 대한 반응 정확도와 평균 반응 시간을 기준으로 점수를 계산했습니다."
                ),
                new TrainingCompletionSummary(
                        null,
                        null,
                        null,
                        "집중력 " + level + "단계",
                        "집중력 훈련 완료",
                        correctCount,
                        totalCount,
                        accuracyRate,
                        wrongCount,
                        level,
                        averageReactionMs
                ),
                new TrainingCompletionProgress(currentLevel, highestUnlockedLevel, level, accuracyRate, averageReactionMs),
                () -> focusTrainingRepository.saveReactionLogs(sessionId, reactions)
        ));

        return new CompleteFocusSessionResponse(
                result.sessionId(),
                result.score(),
                accuracyRate,
                wrongCount,
                averageReactionMs,
                unlockedNextLevel,
                currentLevel,
                highestUnlockedLevel
        );
    }

    private void ensureCompletionDependencies() {
        if (sessionOwnershipValidator == null || trainingCompletionService == null) {
            throw new TrainingServiceException(ErrorCode.INTERNAL_SERVER_ERROR, "Focus completion is not configured.");
        }
    }
}
