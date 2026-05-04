package com.didgo.trainingservice.external.openai;

public class OpenAiAdapterException extends RuntimeException {

    public OpenAiAdapterException(String message) {
        super(message);
    }

    public OpenAiAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
