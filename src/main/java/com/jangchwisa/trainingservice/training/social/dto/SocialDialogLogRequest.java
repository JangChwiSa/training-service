package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialDialogSpeaker;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

public record SocialDialogLogRequest(
        @Positive int turnNo,
        @NotNull SocialDialogSpeaker speaker,
        @NotBlank String content
) {
}
