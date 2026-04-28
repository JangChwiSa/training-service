package com.jangchwisa.trainingservice.training.focus.service;

import com.jangchwisa.trainingservice.common.exception.ErrorCode;
import com.jangchwisa.trainingservice.common.exception.TrainingServiceException;
import com.jangchwisa.trainingservice.common.security.CurrentUser;
import com.jangchwisa.trainingservice.training.focus.dto.FocusCommandResponse;
import com.jangchwisa.trainingservice.training.focus.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.focus.dto.StartFocusSessionResponse;
import com.jangchwisa.trainingservice.training.focus.repository.FocusTrainingRepository;
import com.jangchwisa.trainingservice.training.focus.repository.FocusTrainingRepository.FocusLevelRuleRow;
import com.jangchwisa.trainingservice.training.focus.repository.FocusTrainingRepository.NewFocusCommand;
import com.jangchwisa.trainingservice.training.session.entity.TrainingSession;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.session.service.CreateTrainingSessionCommand;
import com.jangchwisa.trainingservice.training.session.service.TrainingSessionService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FocusTrainingService {

    private final FocusTrainingRepository focusTrainingRepository;
    private final TrainingSessionService trainingSessionService;
    private final FocusCommandGenerator focusCommandGenerator;

    public FocusTrainingService(
            FocusTrainingRepository focusTrainingRepository,
            TrainingSessionService trainingSessionService,
            FocusCommandGenerator focusCommandGenerator
    ) {
        this.focusTrainingRepository = focusTrainingRepository;
        this.trainingSessionService = trainingSessionService;
        this.focusCommandGenerator = focusCommandGenerator;
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
}
