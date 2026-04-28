package com.jangchwisa.trainingservice.common.exception;

public class TrainingServiceException extends RuntimeException {

    private final ErrorCode errorCode;

    public TrainingServiceException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage());
    }

    public TrainingServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
