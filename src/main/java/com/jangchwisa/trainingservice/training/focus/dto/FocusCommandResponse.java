package com.jangchwisa.trainingservice.training.focus.dto;

public record FocusCommandResponse(
        long commandId,
        int order,
        String commandText,
        String expectedAction,
        int displayAtMs
) {
}
