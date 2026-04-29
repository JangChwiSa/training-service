package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialDialogSpeaker;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialDialogLogResponse(
        @Schema(description = "대화 턴 번호입니다.", example = "1")
        int turnNo,
        @Schema(description = "발화자입니다.", example = "USER")
        SocialDialogSpeaker speaker,
        @Schema(description = "저장된 대화 내용입니다.")
        String content
) {
}
