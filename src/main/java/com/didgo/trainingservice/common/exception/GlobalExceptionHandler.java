package com.didgo.trainingservice.common.exception;

import com.didgo.trainingservice.common.response.ApiResponse;
import com.didgo.trainingservice.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TrainingServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleTrainingServiceException(TrainingServiceException exception) {
        if (exception.errorCode().status().is5xxServerError()) {
            log.error(
                    "Training service exception handled. errorCode={}, status={}",
                    exception.errorCode(),
                    exception.errorCode().status().value(),
                    exception
            );
        } else {
            log.warn(
                    "Training service exception handled. errorCode={}, status={}",
                    exception.errorCode(),
                    exception.errorCode().status().value()
            );
        }
        return error(exception.errorCode(), exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        log.warn("Request validation failed. exception={}", exception.getClass().getSimpleName());
        return error(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.warn("Unsupported request method. method={}", exception.getMethod());
        return error(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.defaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        log.error("Unhandled exception.", exception);
        return error(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.defaultMessage());
    }

    private ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode, String message) {
        ErrorResponse error = new ErrorResponse(errorCode.name(), message);
        return ResponseEntity
                .status(errorCode.status())
                .body(ApiResponse.error(error));
    }
}
