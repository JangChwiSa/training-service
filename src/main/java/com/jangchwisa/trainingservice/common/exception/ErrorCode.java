package com.jangchwisa.trainingservice.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access is denied."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Request validation failed."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Requested resource was not found."),
    CONFLICT(HttpStatus.CONFLICT, "Request conflicts with the current state."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
