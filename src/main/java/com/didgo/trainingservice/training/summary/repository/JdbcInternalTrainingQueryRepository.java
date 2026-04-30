package com.didgo.trainingservice.training.summary.repository;

import com.didgo.trainingservice.training.session.entity.TrainingType;
import com.didgo.trainingservice.training.summary.dto.InternalTrainingSummaryResponse;
import com.didgo.trainingservice.training.summary.dto.LatestTrainingResultResponse;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcInternalTrainingQueryRepository implements InternalTrainingQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcInternalTrainingQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public InternalTrainingSummaryResponse findTrainingSummary(long userId) {
        return new InternalTrainingSummaryResponse(
                findSocialRecentScore(userId),
                findIntegerValue("SELECT correct_count FROM user_safety_progress WHERE user_id = ?", userId, 0),
                findIntegerValue("SELECT total_count FROM user_safety_progress WHERE user_id = ?", userId, 0),
                findIntegerValue("SELECT correct_count FROM user_document_progress WHERE user_id = ?", userId, 0),
                findIntegerValue("SELECT total_count FROM user_document_progress WHERE user_id = ?", userId, 0),
                findIntegerValue("SELECT current_level FROM user_focus_progress WHERE user_id = ?", userId, 1)
        );
    }

    @Override
    public List<LatestTrainingResultResponse> findLatestTrainingResults(long userId) {
        String sql = """
                SELECT ts.session_id, ts.training_type, score.score, score.score_type, ts.ended_at
                FROM training_sessions ts
                JOIN training_scores score ON score.session_id = ts.session_id
                WHERE ts.user_id = ?
                  AND ts.status = 'COMPLETED'
                  AND ts.ended_at IS NOT NULL
                  AND EXISTS (
                      SELECT 1
                      FROM training_feedbacks feedback
                      WHERE feedback.session_id = ts.session_id
                  )
                ORDER BY ts.ended_at DESC
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new LatestTrainingResultResponse(
                resultSet.getLong("session_id"),
                TrainingType.valueOf(resultSet.getString("training_type")),
                resultSet.getInt("score"),
                resultSet.getString("score_type"),
                resultSet.getTimestamp("ended_at").toLocalDateTime()
        ), userId);
    }

    private Integer findSocialRecentScore(long userId) {
        List<Integer> values = jdbcTemplate.queryForList(
                "SELECT recent_score FROM user_social_progress WHERE user_id = ?",
                Integer.class,
                userId
        );
        if (values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }

    private int findIntegerValue(String sql, long userId, int defaultValue) {
        List<Integer> values = jdbcTemplate.queryForList(sql, Integer.class, userId);
        if (values.isEmpty()) {
            return defaultValue;
        }
        Integer value = values.getFirst();
        return value == null ? defaultValue : value;
    }
}
