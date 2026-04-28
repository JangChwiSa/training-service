package com.jangchwisa.trainingservice.training.social.dto;

import com.jangchwisa.trainingservice.training.social.entity.SocialDialogSpeaker;

public record SocialDialogLogResponse(
        int turnNo,
        SocialDialogSpeaker speaker,
        String content
) {
}
