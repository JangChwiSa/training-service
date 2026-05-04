package com.didgo.trainingservice.training.score.repository;

import com.didgo.trainingservice.training.score.entity.TrainingScore;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrainingScoreRepository implements TrainingScoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTrainingScoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(TrainingScore trainingScore) {
        String sql = """
                INSERT INTO training_scores (
                    session_id, score, score_type, correct_count, total_count,
                    accuracy_rate, wrong_count, average_reaction_ms, raw_metrics_json, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                trainingScore.sessionId(),
                trainingScore.score(),
                trainingScore.scoreType(),
                trainingScore.correctCount(),
                trainingScore.totalCount(),
                trainingScore.accuracyRate(),
                trainingScore.wrongCount(),
                trainingScore.averageReactionMs(),
                trainingScore.rawMetricsJson(),
                Timestamp.valueOf(trainingScore.createdAt())
        );
    }
}
