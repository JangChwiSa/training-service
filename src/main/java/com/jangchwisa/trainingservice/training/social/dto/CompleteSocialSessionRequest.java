package com.jangchwisa.trainingservice.training.social.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CompleteSocialSessionRequest(
        @NotEmpty List<@Valid SocialDialogLogRequest> dialogLogs
) {
}
