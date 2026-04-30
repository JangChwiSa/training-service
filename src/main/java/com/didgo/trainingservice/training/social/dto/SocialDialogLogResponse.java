package com.didgo.trainingservice.training.social.dto;

import com.didgo.trainingservice.training.social.entity.SocialDialogSpeaker;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialDialogLogResponse(
        @Schema(description = "?????踰덊샇?낅땲??", example = "1")
        int turnNo,
        @Schema(description = "諛쒗솕?먯엯?덈떎.", example = "USER")
        SocialDialogSpeaker speaker,
        @Schema(description = "??λ맂 ????댁슜?낅땲??")
        String content
) {
}
