package com.jangchwisa.trainingservice.training.document.dto;

import java.util.List;

public record SubmitDocumentAnswersResponse(
        long sessionId,
        int score,
        int correctCount,
        int totalCount,
        List<DocumentAnswerResultResponse> results,
        boolean completed
) {
}
