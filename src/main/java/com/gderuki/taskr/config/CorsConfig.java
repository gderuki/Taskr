package com.gderuki.taskr.config;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static com.gderuki.taskr.config.ApiConstants.API_PATH_PATTERN;

/**
 * CORS configuration for frontend integration.
 * Allows configuration of allowed origins, methods, and headers per environment.
 * All values must be configured in application.yml per profile.
 */
@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsConfig implements WebMvcConfigurer {

    /**
     * List of allowed origins for CORS requests.
     */
    private List<String> allowedOrigins;

    /**
     * List of allowed HTTP methods for CORS requests.
     */
    private List<String> allowedMethods;

    /**
     * List of allowed headers for CORS requests.
     */
    private List<String> allowedHeaders;

    /**
     * List of headers that can be exposed to the frontend.
     */
    private List<String> exposedHeaders;

    /**
     * Whether to allow credentials (cookies, authorization headers) in CORS requests.
     */
    private boolean allowCredentials;

    /**
     * Maximum age (in seconds) for caching preflight request results.
     */
    private long maxAge;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping(API_PATH_PATTERN)
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods(allowedMethods.toArray(String[]::new))
                .allowedHeaders(allowedHeaders.toArray(String[]::new))
                .exposedHeaders(exposedHeaders.toArray(String[]::new))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }
}
