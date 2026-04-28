package com.jangchwisa.trainingservice.training.focus.dto;

import java.util.List;

public record StartFocusSessionResponse(
        long sessionId,
        int level,
        int durationSeconds,
        List<FocusCommandResponse> commands
) {
}
