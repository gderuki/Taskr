package com.gderuki.taskr.config;

import com.gderuki.taskr.interceptor.LoggingInterceptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.gderuki.taskr.config.ApiConstants.API_PATH_PATTERN;

/**
 * Web MVC configuration for the application.
 * Registers interceptors and configures web-related settings.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns(API_PATH_PATTERN);
    }
}
