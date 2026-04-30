package com.jangchwisa.trainingservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractMySqlIntegrationTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("training_db")
            .withUsername("training_user")
            .withPassword("training_password");

    static {
        mysql.start();
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }

    protected void cleanMutableTables() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            for (String table : mutableTables()) {
                jdbcTemplate.execute("DELETE FROM " + table);
                jdbcTemplate.execute("ALTER TABLE " + table + " AUTO_INCREMENT = 1");
            }
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private String[] mutableTables() {
        return new String[]{
                "outbox_events",
                "training_session_summaries",
                "training_feedbacks",
                "training_scores",
                "document_answer_logs",
                "document_session_questions",
                "document_questions",
                "focus_reaction_logs",
                "focus_commands",
                "focus_level_rules",
                "safety_action_logs",
                "safety_choices",
                "safety_scenes",
                "safety_scenarios",
                "social_dialog_logs",
                "social_scenarios",
                "user_document_progress",
                "user_focus_progress",
                "user_safety_progress",
                "user_social_progress",
                "training_sessions"
        };
    }
}
