package com.gderuki.taskr.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Configuration for custom health indicators.
 * Spring Boot 4 requires explicit registration of health checks as HealthContributor beans.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthConfig {

    private static final long PERFORMANCE_THRESHOLD_MS = 50;

    private final DataSource dataSource;

    /**
     * Custom database health indicator that checks PostgreSQL connectivity
     * and measures query execution time.
     * <p>
     * Exposed at: /actuator/health under "database"
     */
    @Bean
    public HealthContributor database() {
        return (HealthIndicator) () -> {
            try {
                long startTime = System.currentTimeMillis();

                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery("SELECT version()")) {

                    long queryTime = System.currentTimeMillis() - startTime;

                    if (resultSet.next()) {
                        String dbVersion = resultSet.getString(1);
                        String performanceStatus = queryTime < PERFORMANCE_THRESHOLD_MS ? "good" : "degraded";

                        return Health.up()
                                .withDetail("database", "PostgreSQL")
                                .withDetail("version", dbVersion)
                                .withDetail("catalog", connection.getCatalog())
                                .withDetail("query_time_ms", queryTime)
                                .withDetail("connection_valid", connection.isValid(5))
                                .withDetail("readonly_mode", connection.isReadOnly())
                                .withDetail("auto_commit", connection.getAutoCommit())
                                .withDetail("performance_status", performanceStatus)
                                .build();
                    } else {
                        return Health.down()
                                .withDetail("database", "PostgreSQL")
                                .withDetail("error", "Query returned no results")
                                .build();
                    }
                }
            } catch (Exception e) {
                log.error("Database health check failed", e);
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", e.getClass().getSimpleName())
                        .withDetail("message", e.getMessage())
                        .build();
            }
        };
    }
}
