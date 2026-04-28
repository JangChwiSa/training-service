package com.jangchwisa.trainingservice.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
final class LocalDatabaseConnectionVerifier implements ApplicationRunner {

    private final Environment environment;

    LocalDatabaseConnectionVerifier(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        String host = environment.getProperty("DB_HOST", "localhost");
        String port = environment.getProperty("DB_PORT", "3306");
        String database = environment.getProperty("DB_NAME", "training_db");
        String username = environment.getProperty("DB_USERNAME", "training_user");
        String password = environment.getProperty("DB_PASSWORD", "training_password");
        String url = "jdbc:mysql://%s:%s/%s?connectTimeout=5000&socketTimeout=5000".formatted(host, port, database);

        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            // Verifies that the local Training Service container can reach training_db on startup.
        }
    }
}
