package com.jangchwisa.trainingservice.training.progress.repository;

import com.jangchwisa.trainingservice.training.progress.dto.DocumentProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.FocusProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SafetyProgressResponse;
import com.jangchwisa.trainingservice.training.progress.dto.SocialProgressResponse;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
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
