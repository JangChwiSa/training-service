package com.didgo.trainingservice.training.document.repository;

import com.didgo.trainingservice.training.document.dto.DocumentAnswerRequest;
import com.didgo.trainingservice.training.document.dto.DocumentAnswerDetailResponse;
import com.didgo.trainingservice.training.document.dto.DocumentQuestionResponse;
import java.util.List;
import java.util.Map;
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

    default Optional<DocumentFeedbackRow> findFeedback(long sessionId) {
        throw new UnsupportedOperationException("findFeedback is not implemented.");
    }

    List<DocumentAnswerDetailResponse> findAnswerLogs(long sessionId);

    record DocumentScoreRow(int score, int correctCount, int totalCount) {
    }

    record DocumentFeedbackRow(String summary, String detailText) {
    }

    record DocumentQuestionAnswerRow(
            long questionId,
            String title,
            String questionText,
            String questionType,
            String correctAnswer,
            String explanation,
            Long correctChoiceId,
            Map<Long, String> choiceTextById
    ) {
        public DocumentQuestionAnswerRow(
                long questionId,
                String title,
                String questionText,
                String correctAnswer,
                String explanation
        ) {
            this(questionId, title, questionText, "SHORT_ANSWER", correctAnswer, explanation, null, Map.of());
        }
    }

    record ScoredDocumentAnswer(
            long questionId,
            String questionText,
            String userAnswer,
            String correctAnswer,
            boolean correct,
            String explanation
    ) {

        public static ScoredDocumentAnswer from(DocumentAnswerRequest request, DocumentQuestionAnswerRow question) {
            if ("MULTIPLE_CHOICE".equalsIgnoreCase(question.questionType())) {
                if (request.choiceId() == null) {
                    throw new com.didgo.trainingservice.common.exception.TrainingServiceException(
                            com.didgo.trainingservice.common.exception.ErrorCode.VALIDATION_ERROR,
                            "Document multiple choice answer requires choiceId."
                    );
                }
                String selectedChoiceText = question.choiceTextById().get(request.choiceId());
                if (selectedChoiceText == null) {
                    throw new com.didgo.trainingservice.common.exception.TrainingServiceException(
                            com.didgo.trainingservice.common.exception.ErrorCode.VALIDATION_ERROR,
                            "Document answer choiceId is invalid."
                    );
                }
                boolean correct = request.choiceId().equals(question.correctChoiceId());
                return new ScoredDocumentAnswer(
                        request.questionId(),
                        question.questionText(),
                        selectedChoiceText,
                        question.correctAnswer(),
                        correct,
                        question.explanation()
                );
            }
            if (request.userAnswer() == null || request.userAnswer().isBlank()) {
                throw new com.didgo.trainingservice.common.exception.TrainingServiceException(
                        com.didgo.trainingservice.common.exception.ErrorCode.VALIDATION_ERROR,
                        "Document short answer requires userAnswer."
                );
            }
            boolean correct = question.correctAnswer().trim().equalsIgnoreCase(request.userAnswer().trim());
            return new ScoredDocumentAnswer(
                    request.questionId(),
                    question.questionText(),
                    request.userAnswer(),
                    question.correctAnswer(),
                    correct,
                    question.explanation()
            );
        }
    }
}
