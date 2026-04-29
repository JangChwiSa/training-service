package com.jangchwisa.trainingservice.training.document.repository;

import com.jangchwisa.trainingservice.training.document.dto.DocumentAnswerDetailResponse;
import com.jangchwisa.trainingservice.training.document.dto.DocumentQuestionResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcDocumentTrainingRepository implements DocumentTrainingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDocumentTrainingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DocumentQuestionResponse> findActiveQuestions() {
        String sql = """
                SELECT question_id, title, document_text, question_text, question_type
                FROM document_questions
                WHERE is_active = true
                ORDER BY question_id ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentQuestionResponse(
                resultSet.getLong("question_id"),
                resultSet.getString("title"),
                resultSet.getString("document_text"),
                resultSet.getString("question_text"),
                resultSet.getString("question_type")
        ));
    }

    @Override
    public List<DocumentQuestionResponse> findRandomActiveQuestionsByDifficulty(String difficulty, int limit) {
        String sql = """
                SELECT question_id, title, document_text, question_text, question_type
                FROM document_questions
                WHERE difficulty = ?
                  AND is_active = true
                ORDER BY RAND()
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentQuestionResponse(
                resultSet.getLong("question_id"),
                resultSet.getString("title"),
                resultSet.getString("document_text"),
                resultSet.getString("question_text"),
                resultSet.getString("question_type")
        ), difficulty, limit);
    }

    @Override
    public void saveSessionQuestions(long sessionId, List<DocumentQuestionResponse> questions) {
        String sql = """
                INSERT INTO document_session_questions (
                    session_id, question_id, display_order, created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP(6))
                """;
        for (int index = 0; index < questions.size(); index++) {
            jdbcTemplate.update(sql, sessionId, questions.get(index).questionId(), index + 1);
        }
    }

    @Override
    public List<Long> findSessionQuestionIds(long sessionId) {
        String sql = """
                SELECT question_id
                FROM document_session_questions
                WHERE session_id = ?
                ORDER BY display_order ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> resultSet.getLong("question_id"), sessionId);
    }

    @Override
    public List<DocumentQuestionAnswerRow> findAssignedQuestionAnswers(long sessionId) {
        String sql = """
                SELECT question.question_id,
                       question.title,
                       question.question_text,
                       question.correct_answer,
                       question.explanation
                FROM document_session_questions session_question
                JOIN document_questions question ON question.question_id = session_question.question_id
                WHERE session_question.session_id = ?
                ORDER BY session_question.display_order ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentQuestionAnswerRow(
                resultSet.getLong("question_id"),
                resultSet.getString("title"),
                resultSet.getString("question_text"),
                resultSet.getString("correct_answer"),
                resultSet.getString("explanation")
        ), sessionId);
    }

    @Override
    public List<DocumentQuestionAnswerRow> findQuestionAnswers(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", questionIds.stream().map(id -> "?").toList());
        String sql = """
                SELECT question_id, title, question_text, correct_answer, explanation
                FROM document_questions
                WHERE question_id IN (%s)
                  AND is_active = true
                """.formatted(placeholders);
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentQuestionAnswerRow(
                resultSet.getLong("question_id"),
                resultSet.getString("title"),
                resultSet.getString("question_text"),
                resultSet.getString("correct_answer"),
                resultSet.getString("explanation")
        ), questionIds.toArray());
    }

    @Override
    public void saveAnswerLogs(long sessionId, List<ScoredDocumentAnswer> answers) {
        String sql = """
                INSERT INTO document_answer_logs (
                    session_id, question_id, user_answer, correct_answer, is_correct, explanation, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(6))
                """;
        for (ScoredDocumentAnswer answer : answers) {
            jdbcTemplate.update(
                    sql,
                    sessionId,
                    answer.questionId(),
                    answer.userAnswer(),
                    answer.correctAnswer(),
                    answer.correct(),
                    answer.explanation()
            );
        }
    }

    @Override
    public Optional<DocumentScoreRow> findScore(long sessionId) {
        String sql = """
                SELECT score, correct_count, total_count
                FROM training_scores
                WHERE session_id = ?
                """;
        List<DocumentScoreRow> scores = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentScoreRow(
                resultSet.getInt("score"),
                resultSet.getInt("correct_count"),
                resultSet.getInt("total_count")
        ), sessionId);
        return scores.stream().findFirst();
    }

    @Override
    public Optional<DocumentFeedbackRow> findFeedback(long sessionId) {
        String sql = """
                SELECT summary, detail_text
                FROM training_feedbacks
                WHERE session_id = ?
                  AND feedback_type = 'SUMMARY'
                ORDER BY created_at DESC
                LIMIT 1
                """;
        List<DocumentFeedbackRow> feedbacks = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentFeedbackRow(
                resultSet.getString("summary"),
                resultSet.getString("detail_text")
        ), sessionId);
        return feedbacks.stream().findFirst();
    }

    @Override
    public List<DocumentAnswerDetailResponse> findAnswerLogs(long sessionId) {
        String sql = """
                SELECT answer.question_id,
                       question.question_text,
                       answer.user_answer,
                       answer.correct_answer,
                       answer.is_correct,
                       answer.explanation
                FROM document_answer_logs answer
                JOIN document_questions question ON question.question_id = answer.question_id
                WHERE answer.session_id = ?
                ORDER BY answer.answer_id ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentAnswerDetailResponse(
                resultSet.getLong("question_id"),
                resultSet.getString("question_text"),
                resultSet.getString("user_answer"),
                resultSet.getString("correct_answer"),
                resultSet.getBoolean("is_correct"),
                resultSet.getString("explanation")
        ), sessionId);
    }
}
