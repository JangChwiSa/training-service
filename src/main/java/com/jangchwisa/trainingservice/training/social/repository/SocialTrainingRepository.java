package com.jangchwisa.trainingservice.training.social.repository;

import com.jangchwisa.trainingservice.training.social.dto.SocialDialogLogResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialFeedbackResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioDetailResponse;
import com.jangchwisa.trainingservice.training.social.dto.SocialScenarioListItemResponse;
import com.jangchwisa.trainingservice.training.social.entity.SocialJobType;
import java.util.List;
import java.util.Optional;

public interface SocialTrainingRepository {

    List<SocialScenarioListItemResponse> findActiveScenariosByJobType(SocialJobType jobType);

    Optional<SocialScenarioDetailResponse> findActiveScenarioDetail(long scenarioId);

    boolean existsActiveScenario(long scenarioId, SocialJobType jobType);

    Optional<SocialScoreRow> findScore(long sessionId);

    Optional<SocialFeedbackResponse> findFeedback(long sessionId);

    List<SocialDialogLogResponse> findDialogLogs(long sessionId);

    record SocialScoreRow(int score, String scoreType) {
    }
}
