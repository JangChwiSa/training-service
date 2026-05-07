package com.didgo.trainingservice.training.social.dto;

import com.didgo.trainingservice.training.social.voice.dto.VoiceSummaryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CompleteSocialSessionRequest(
        @Schema(description = "?ы쉶???덈젴 以??꾩쟻???꾩껜 ???濡쒓렇?낅땲?? 理쒖냼 1媛??댁긽 ?꾩슂?⑸땲??")
        @NotEmpty List<@Valid SocialDialogLogRequest> dialogLogs,
        @Schema(description = "Realtime voice session metadata. Null for text-only completion.")
        @Valid VoiceSummaryRequest voiceSummary
) {
    public CompleteSocialSessionRequest(List<SocialDialogLogRequest> dialogLogs) {
        this(dialogLogs, null);
    }
}
