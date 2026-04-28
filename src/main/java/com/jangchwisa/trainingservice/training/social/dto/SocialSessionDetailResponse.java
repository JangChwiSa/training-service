package com.jangchwisa.trainingservice.training.social.dto;

import java.util.List;

public record SocialSessionDetailResponse(
        long sessionId,
        int score,
        String scoreType,
        SocialFeedbackResponse feedback,
        List<SocialDialogLogResponse> dialogLogs
) {
}
