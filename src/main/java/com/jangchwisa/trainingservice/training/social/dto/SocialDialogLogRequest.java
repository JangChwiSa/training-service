package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialDialogSpeaker;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

public record SocialDialogLogRequest(
        @Schema(description = "대화 턴 번호입니다. 같은 턴의 사용자 발화와 AI 응답은 같은 번호를 사용할 수 있습니다.", example = "1")
        @Positive int turnNo,
        @Schema(description = "발화자입니다. USER는 사용자, AI는 AI 응답입니다.", example = "USER")
        @NotNull SocialDialogSpeaker speaker,
        @Schema(description = "해당 턴의 실제 대화 내용입니다. 빈 문자열은 사용할 수 없습니다.", example = "Can you help me?")
        @NotBlank String content
) {
}
