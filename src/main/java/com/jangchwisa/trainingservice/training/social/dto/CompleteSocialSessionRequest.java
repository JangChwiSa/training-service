package com.jangchwisa.trainingservice.training.social.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CompleteSocialSessionRequest(
        @Schema(description = "사회성 훈련 중 누적한 전체 대화 로그입니다. 최소 1개 이상 필요합니다.")
        @NotEmpty List<@Valid SocialDialogLogRequest> dialogLogs
) {
}
