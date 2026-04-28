package com.jangchwisa.trainingservice.training.document.dto;

import java.util.List;

public record StartDocumentSessionResponse(
        long sessionId,
        List<DocumentQuestionResponse> questions
) {
}
