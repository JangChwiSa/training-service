package com.didgo.trainingservice.common.response;

public record ErrorResponse(
        String code,
        String message
) {
}
