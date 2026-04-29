package com.jangchwisa.trainingservice.training.progress.repository;

import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.progress.entity.MonthlyTrainingSummaryEntry;
import com.jangchwisa.trainingservice.training.progress.entity.TrainingProgressCompletion;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrainingProgressRepository implements TrainingProgressRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTrainingProgressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void applyCompletion(TrainingProgressCompletion completion) {
        switch (completion.trainingType()) {
            case SOCIAL -> applySocialCompletion(completion);
            case SAFETY -> applySafetyCompletion(completion);
            case DOCUMENT -> applyDocumentCompletion(completion);
            case FOCUS -> applyFocusCompletion(completion);
        }
    }

    private void applySocialCompletion(TrainingProgressCompletion completion) {
        String updateSql = """
                UPDATE user_social_progress
                SET recent_session_id = ?,
                    recent_score = ?,
                    recent_feedback_summary = ?,
                    completed_count = completed_count + 1,
                    last_completed_at = ?,
                    updated_at = ?
                WHERE user_id = ?
                """;
        int updatedRows = jdbcTemplate.update(
                updateSql,
                completion.sessionId(),
                completion.score(),
                completion.feedbackSummary(),
                Timestamp.valueOf(completion.completedAt()),
                Timestamp.valueOf(completion.completedAt()),
                completion.userId()
        );
        if (updatedRows == 0) {
            String insertSql = """
                    INSERT INTO user_social_progress (
                        user_id, recent_session_id, recent_score, recent_feedback_summary,
                        completed_count, last_completed_at, updated_at
                    )
                    VALUES (?, ?, ?, ?, 1, ?, ?)
                    """;
            jdbcTemplate.update(
                    insertSql,
                    completion.userId(),
                    completion.sessionId(),
                    completion.score(),
                    completion.feedbackSummary(),
                    Timestamp.valueOf(completion.completedAt()),
                    Timestamp.valueOf(completion.completedAt())
            );
        }
    }

    private void applySafetyCompletion(TrainingProgressCompletion completion) {
        String updateSql = """
                UPDATE user_safety_progress
                SET recent_session_id = ?,
                    correct_count = ?,
                    total_count = ?,
                    completed_count = completed_count + 1,
                    last_completed_at = ?,
                    updated_at = ?
                WHERE user_id = ?
                """;
        int updatedRows = jdbcTemplate.update(
                updateSql,
                completion.sessionId(),
                completion.correctCount(),
                completion.totalCount(),
                Timestamp.valueOf(completion.completedAt()),
                Timestamp.valueOf(completion.completedAt()),
                completion.userId()
        );
        if (updatedRows == 0) {
            String insertSql = """
                    INSERT INTO user_safety_progress (
                        user_id, recent_session_id, correct_count, total_count,
                        completed_count, last_completed_at, updated_at
                    )
                    VALUES (?, ?, ?, ?, 1, ?, ?)
                    """;
            jdbcTemplate.update(
                    insertSql,
                    completion.userId(),
                    completion.sessionId(),
                    completion.correctCount(),
                    completion.totalCount(),
                    Timestamp.valueOf(completion.completedAt()),
                    Timestamp.valueOf(completion.completedAt())
            );
        }
    }

    private void applyDocumentCompletion(TrainingProgressCompletion completion) {
        String updateSql = """
                UPDATE user_document_progress
                SET recent_session_id = ?,
                    correct_count = ?,
                    total_count = ?,
                    recent_score = ?,
                    completed_count = completed_count + 1,
                    last_completed_at = ?,
                    updated_at = ?
                WHERE user_id = ?
                """;
        int updatedRows = jdbcTemplate.update(
                updateSql,
                completion.sessionId(),
                completion.correctCount(),
                completion.totalCount(),
                completion.score(),
                Timestamp.valueOf(completion.completedAt()),
                Timestamp.valueOf(completion.completedAt()),
                completion.userId()
        );
        if (updatedRows == 0) {
            String insertSql = """
                    INSERT INTO user_document_progress (
                        user_id, recent_session_id, correct_count, total_count, recent_score,
                        completed_count, last_completed_at, updated_at
                    )
                    VALUES (?, ?, ?, ?, ?, 1, ?, ?)
                    """;
            jdbcTemplate.update(
                    insertSql,
                    completion.userId(),
                    completion.sessionId(),
                    completion.correctCount(),
                    completion.totalCount(),
                    completion.score(),
                    Timestamp.valueOf(completion.completedAt()),
                    Timestamp.valueOf(completion.completedAt())
            );
        }
    }

    private void applyFocusCompletion(TrainingProgressCompletion completion) {
        String updateSql = """
                UPDATE user_focus_progress
                SET current_level = ?,
                    highest_unlocked_level = ?,
                    last_played_level = ?,
                    last_accuracy_rate = ?,
                    last_average_reaction_ms = ?,
                    updated_at = ?
                WHERE user_id = ?
                """;
        int updatedRows = jdbcTemplate.update(
                updateSql,
                completion.currentLevel(),
                completion.highestUnlockedLevel(),
                completion.playedLevel(),
                completion.accuracyRate(),
                completion.averageReactionMs(),
                Timestamp.valueOf(completion.completedAt()),
                completion.userId()
        );
        if (updatedRows == 0) {
            String insertSql = """
                    INSERT INTO user_focus_progress (
                        user_id, current_level, highest_unlocked_level, last_played_level,
                        last_accuracy_rate, last_average_reaction_ms, updated_at
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
            jdbcTemplate.update(
                    insertSql,
                    completion.userId(),
                    completion.currentLevel(),
                    completion.highestUnlockedLevel(),
                    completion.playedLevel(),
                    completion.accuracyRate(),
                    completion.averageReactionMs(),
                    Timestamp.valueOf(completion.completedAt())
            );
        }
    }

    @Override
    public List<MonthlyTrainingSummaryEntry> findMonthlyCompletedSummaries(
            long userId,
            TrainingType trainingType,
            LocalDateTime periodStart,
            LocalDateTime periodEnd
    ) {
        String sql = """
                SELECT s.score, s.category, s.played_level, ts.sub_type
                FROM training_session_summaries s
                JOIN training_sessions ts ON ts.session_id = s.session_id
                WHERE s.user_id = ?
                  AND s.training_type = ?
                  AND s.completed_at >= ?
                  AND s.completed_at < ?
                ORDER BY s.completed_at ASC
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new MonthlyTrainingSummaryEntry(
                nullableInteger(resultSet.getObject("score")),
                resultSet.getString("category"),
                nullableInteger(resultSet.getObject("played_level")),
                resultSet.getString("sub_type")
        ), userId, trainingType.name(), Timestamp.valueOf(periodStart), Timestamp.valueOf(periodEnd));
    }

    @Override
    public Optional<SocialProgressResponse> findSocialProgress(long userId) {
        String sql = """
                SELECT recent_session_id, recent_score, recent_feedback_summary,
                       completed_count, last_completed_at
                FROM user_social_progress
                WHERE user_id = ?
                """;
        List<SocialProgressResponse> results = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SocialProgressResponse(
                TrainingType.SOCIAL,
                nullableLong(resultSet.getObject("recent_session_id")),
                nullableInteger(resultSet.getObject("recent_score")),
                resultSet.getString("recent_feedback_summary"),
                resultSet.getInt("completed_count"),
                resultSet.getTimestamp("last_completed_at") == null
                        ? null
                        : resultSet.getTimestamp("last_completed_at").toLocalDateTime()
        ), userId);
        return results.stream().findFirst();
    }

    @Override
    public Optional<SafetyProgressResponse> findSafetyProgress(long userId) {
        String sql = """
                SELECT recent_session_id, correct_count, total_count,
                       completed_count, last_completed_at
                FROM user_safety_progress
                WHERE user_id = ?
                """;
        List<SafetyProgressResponse> results = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new SafetyProgressResponse(
                TrainingType.SAFETY,
                nullableLong(resultSet.getObject("recent_session_id")),
                resultSet.getInt("correct_count"),
                resultSet.getInt("total_count"),
                resultSet.getInt("completed_count"),
                resultSet.getTimestamp("last_completed_at") == null
                        ? null
                        : resultSet.getTimestamp("last_completed_at").toLocalDateTime()
        ), userId);
        return results.stream().findFirst();
    }

    @Override
    public Optional<DocumentProgressResponse> findDocumentProgress(long userId) {
        String sql = """
                SELECT recent_session_id, correct_count, total_count, recent_score,
                       completed_count, last_completed_at
                FROM user_document_progress
                WHERE user_id = ?
                """;
        List<DocumentProgressResponse> results = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new DocumentProgressResponse(
                TrainingType.DOCUMENT,
                nullableLong(resultSet.getObject("recent_session_id")),
                resultSet.getInt("correct_count"),
                resultSet.getInt("total_count"),
                nullableInteger(resultSet.getObject("recent_score")),
                resultSet.getInt("completed_count"),
                resultSet.getTimestamp("last_completed_at") == null
                        ? null
                        : resultSet.getTimestamp("last_completed_at").toLocalDateTime()
        ), userId);
        return results.stream().findFirst();
    }

    @Override
    public Optional<FocusProgressResponse> findFocusProgress(long userId) {
        String sql = """
                SELECT current_level, highest_unlocked_level, last_played_level,
                       last_accuracy_rate, last_average_reaction_ms, updated_at
                FROM user_focus_progress
                WHERE user_id = ?
                """;
        List<FocusProgressResponse> results = jdbcTemplate.query(sql, (resultSet, rowNumber) -> new FocusProgressResponse(
                TrainingType.FOCUS,
                resultSet.getInt("current_level"),
                resultSet.getInt("highest_unlocked_level"),
                nullableInteger(resultSet.getObject("last_played_level")),
                resultSet.getBigDecimal("last_accuracy_rate"),
                nullableInteger(resultSet.getObject("last_average_reaction_ms")),
                resultSet.getTimestamp("updated_at").toLocalDateTime()
        ), userId);
        return results.stream().findFirst();
    }

    private static Long nullableLong(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).longValue();
    }

    private static Integer nullableInteger(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }
}
