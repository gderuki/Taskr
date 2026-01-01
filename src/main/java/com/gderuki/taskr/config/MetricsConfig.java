package com.gderuki.taskr.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application metrics and monitoring.
 * Configures Micrometer metrics with custom tags and enables Prometheus export.
 */
@Configuration
public class MetricsConfig {

    /**
     * Enable @Timed annotation support for method-level metrics.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
