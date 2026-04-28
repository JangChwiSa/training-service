package com.jangchwisa.trainingservice.training.summary.repository;

import com.jangchwisa.trainingservice.training.safety.entity.SafetyCategory;
import com.jangchwisa.trainingservice.training.session.entity.TrainingType;
import com.jangchwisa.trainingservice.training.summary.dto.TrainingSessionListItemResponse;
import com.jangchwisa.trainingservice.training.summary.entity.TrainingSessionSummary;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrainingSessionSummaryRepository implements TrainingSessionSummaryRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTrainingSessionSummaryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(TrainingSessionSummary summary) {
        String sql = """
                INSERT INTO training_session_summaries (
                    session_id, user_id, training_type, scenario_id, scenario_title, category,
                    title, score, summary_text, feedback_summary, correct_count, total_count,
                    accuracy_rate, wrong_count, played_level, average_reaction_ms,
                    completed_at, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                summary.sessionId(),
                summary.userId(),
                summary.trainingType().name(),
                summary.scenarioId(),
                summary.scenarioTitle(),
                summary.category(),
                summary.title(),
                summary.score(),
                summary.summaryText(),
                summary.feedbackSummary(),
                summary.correctCount(),
                summary.totalCount(),
                summary.accuracyRate(),
                summary.wrongCount(),
                summary.playedLevel(),
                summary.averageReactionMs(),
                Timestamp.valueOf(summary.completedAt()),
                Timestamp.valueOf(summary.createdAt())
        );
    }

    @Override
    public long countByUserIdAndTrainingType(long userId, TrainingType trainingType, SafetyCategory category) {
        QueryParts queryParts = buildQuery(
                """
                SELECT COUNT(*)
                FROM training_session_summaries
                """,
                userId,
                trainingType,
                category
        );
        Long count = jdbcTemplate.queryForObject(queryParts.sql(), Long.class, queryParts.parameters().toArray());
        return count == null ? 0 : count;
    }

    @Override
    public List<TrainingSessionListItemResponse> findByUserIdAndTrainingType(
            long userId,
            TrainingType trainingType,
            SafetyCategory category,
            int page,
            int size
    ) {
        QueryParts queryParts = buildQuery(
                """
                SELECT session_id, scenario_id, scenario_title, category, score,
                       feedback_summary, correct_count, total_count, played_level,
                       accuracy_rate, wrong_count, average_reaction_ms, completed_at
                FROM training_session_summaries
                """,
                userId,
                trainingType,
                category
        );
        String sql = queryParts.sql() + """

                ORDER BY completed_at DESC
                LIMIT ? OFFSET ?
                """;
        List<Object> parameters = new ArrayList<>(queryParts.parameters());
        parameters.add(size);
        parameters.add(page * size);

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new TrainingSessionListItemResponse(
                resultSet.getLong("session_id"),
                nullableLong(resultSet.getObject("scenario_id")),
                resultSet.getString("scenario_title"),
                nullableCategory(resultSet.getString("category")),
                nullableInteger(resultSet.getObject("score")),
                resultSet.getString("feedback_summary"),
                nullableInteger(resultSet.getObject("correct_count")),
                nullableInteger(resultSet.getObject("total_count")),
                nullableInteger(resultSet.getObject("played_level")),
                resultSet.getBigDecimal("accuracy_rate"),
                nullableInteger(resultSet.getObject("wrong_count")),
                nullableInteger(resultSet.getObject("average_reaction_ms")),
                resultSet.getTimestamp("completed_at").toLocalDateTime()
        ), parameters.toArray());
    }

    private QueryParts buildQuery(String selectClause, long userId, TrainingType trainingType, SafetyCategory category) {
        StringBuilder sql = new StringBuilder(selectClause)
                .append("""
                WHERE user_id = ?
                  AND training_type = ?
                """);
        List<Object> parameters = new ArrayList<>();
        parameters.add(userId);
        parameters.add(trainingType.name());

        if (trainingType == TrainingType.SAFETY && category != null) {
            sql.append("  AND category = ?\n");
            parameters.add(category.name());
        }

        return new QueryParts(sql.toString(), parameters);
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

    private static SafetyCategory nullableCategory(String value) {
        if (value == null) {
            return null;
        }
        return SafetyCategory.valueOf(value);
    }

    private record QueryParts(String sql, List<Object> parameters) {
    }
}
