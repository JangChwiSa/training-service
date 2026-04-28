package com.jangchwisa.trainingservice.training.focus.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CompleteFocusSessionRequest(
        @NotEmpty List<@Valid FocusReactionRequest> reactions
) {
}
