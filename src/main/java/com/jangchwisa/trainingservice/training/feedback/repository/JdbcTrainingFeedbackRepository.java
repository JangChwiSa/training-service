package com.jangchwisa.trainingservice.training.feedback.repository;

import com.jangchwisa.trainingservice.training.feedback.entity.TrainingFeedback;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcTrainingFeedbackRepository implements TrainingFeedbackRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTrainingFeedbackRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(TrainingFeedback trainingFeedback) {
        String sql = """
                INSERT INTO training_feedbacks (
                    session_id, feedback_type, feedback_source, summary, detail_text, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                sql,
                trainingFeedback.sessionId(),
                trainingFeedback.feedbackType(),
                trainingFeedback.feedbackSource(),
                trainingFeedback.summary(),
                trainingFeedback.detailText(),
                Timestamp.valueOf(trainingFeedback.createdAt())
        );
    }
}
