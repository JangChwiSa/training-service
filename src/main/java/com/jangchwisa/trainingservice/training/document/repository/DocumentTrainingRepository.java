package com.jangchwisa.trainingservice.training.document.repository;

import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentQuestionResponse;
import java.util.List;
import java.util.Optional;

public interface DocumentTrainingRepository {

    List<DocumentQuestionResponse> findActiveQuestions();

    Optional<DocumentScoreRow> findScore(long sessionId);

    List<DocumentAnswerDetailResponse> findAnswerLogs(long sessionId);

    record DocumentScoreRow(int score, int correctCount, int totalCount) {
    }
}
