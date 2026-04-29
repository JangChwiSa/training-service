package com.jangchwisa.trainingservice.training.document.repository;

import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerRequest;
import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentQuestionResponse;
import java.util.List;
import java.util.Optional;

public interface DocumentTrainingRepository {

    List<DocumentQuestionResponse> findActiveQuestions();

    default List<DocumentQuestionResponse> findRandomActiveQuestionsByDifficulty(String difficulty, int limit) {
        throw new UnsupportedOperationException("findRandomActiveQuestionsByDifficulty is not implemented.");
    }

    default void saveSessionQuestions(long sessionId, List<DocumentQuestionResponse> questions) {
        throw new UnsupportedOperationException("saveSessionQuestions is not implemented.");
    }

    default List<Long> findSessionQuestionIds(long sessionId) {
        throw new UnsupportedOperationException("findSessionQuestionIds is not implemented.");
    }

    default List<DocumentQuestionAnswerRow> findAssignedQuestionAnswers(long sessionId) {
        throw new UnsupportedOperationException("findAssignedQuestionAnswers is not implemented.");
    }

    default List<DocumentQuestionAnswerRow> findQuestionAnswers(List<Long> questionIds) {
        throw new UnsupportedOperationException("findQuestionAnswers is not implemented.");
    }

    default void saveAnswerLogs(long sessionId, List<ScoredDocumentAnswer> answers) {
        throw new UnsupportedOperationException("saveAnswerLogs is not implemented.");
    }

    Optional<DocumentScoreRow> findScore(long sessionId);

    List<DocumentAnswerDetailResponse> findAnswerLogs(long sessionId);

    record DocumentScoreRow(int score, int correctCount, int totalCount) {
    }

    record DocumentQuestionAnswerRow(
            long questionId,
            String title,
            String questionText,
            String correctAnswer,
            String explanation
    ) {
    }

    record ScoredDocumentAnswer(
            long questionId,
            String userAnswer,
            String correctAnswer,
            boolean correct,
            String explanation
    ) {

        public static ScoredDocumentAnswer from(DocumentAnswerRequest request, DocumentQuestionAnswerRow question) {
            boolean correct = question.correctAnswer().trim().equalsIgnoreCase(request.userAnswer().trim());
            return new ScoredDocumentAnswer(
                    request.questionId(),
                    request.userAnswer(),
                    question.correctAnswer(),
                    correct,
                    question.explanation()
            );
        }
    }
}
