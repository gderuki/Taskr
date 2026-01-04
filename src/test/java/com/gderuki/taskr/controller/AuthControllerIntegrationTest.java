package com.gderuki.taskr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.LoginRequest;
import com.gderuki.taskr.dto.LoginResponse;
import com.gderuki.taskr.dto.RefreshTokenRequest;
import com.gderuki.taskr.entity.RefreshToken;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.repository.RefreshTokenRepository;
import com.gderuki.taskr.repository.UserRepository;
import com.gderuki.taskr.security.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest extends WithTestContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        refreshTokenRepository.flush();
        userRepository.flush();
        entityManager.clear();
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .enabled(true)
                .build();
        userRepository.save(testUser);
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @Transactional
        @DisplayName("with valid credentials should return tokens and save refresh token")
        void withValidCredentials_ShouldReturnTokens() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "password");

            MvcResult result = mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").isNumber())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);

            String username = jwtUtil.extractUsername(response.getAccessToken());
            assertThat(username).isEqualTo("testuser");

            Optional<RefreshToken> savedToken = refreshTokenRepository.findByToken(response.getRefreshToken());
            assertThat(savedToken).isPresent();
            assertThat(savedToken.get().getUser().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("with invalid password should return 401")
        void withInvalidPassword_ShouldReturn401() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("with non-existent user should return 401")
        void withNonExistentUser_ShouldReturn401() throws Exception {
            LoginRequest request = new LoginRequest("nonexistent", "password");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("with disabled user should return 401")
        void withDisabledUser_ShouldReturn401() throws Exception {
            User disabledUser = User.builder()
                    .username("disabled")
                    .email("disabled@example.com")
                    .password(passwordEncoder.encode("password"))
                    .enabled(false)
                    .build();
            userRepository.save(disabledUser);

            LoginRequest request = new LoginRequest("disabled", "password");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("with empty credentials should return 400 with validation errors")
        void withEmptyCredentials_ShouldReturn400() throws Exception {
            LoginRequest request = new LoginRequest("", "");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationErrors").exists());
        }

        @Test
        @DisplayName("with null username should return 400")
        void withNullUsername_ShouldReturn400() throws Exception {
            String jsonWithNull = "{\"username\": null, \"password\": \"password\"}";

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonWithNull))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("with SQL injection attempt should not cause error")
        void withSqlInjectionAttempt_ShouldNotCauseError() throws Exception {
            LoginRequest request = new LoginRequest("admin' OR '1'='1", "password");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("JWT Token Validation")
    class JwtTokenValidationTests {

        @Test
        @Transactional
        @DisplayName("should return valid JWT with correct expiration")
        void shouldReturnValidJwtWithCorrectExpiration() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "password");

            MvcResult result = mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);

            assertThat(jwtUtil.validateToken(response.getAccessToken(),
                    new org.springframework.security.core.userdetails.User(
                            "testuser", "password", java.util.Collections.emptyList()))).isTrue();

            java.util.Date expiration = jwtUtil.extractExpiration(response.getAccessToken());
            assertThat(expiration).isAfter(new java.util.Date());

            long expectedExpiresIn = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            assertThat(response.getExpiresIn()).isBetween(expectedExpiresIn - 5, expectedExpiresIn + 5);
        }

        @Test
        @Transactional
        @DisplayName("should not return password in response")
        void shouldNotReturnPasswordInResponse() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "password");

            MvcResult result = mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).doesNotContain("password");
            assertThat(responseBody).doesNotContain(testUser.getPassword());
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("with valid token should return new tokens and rotate refresh token")
        void withValidToken_ShouldReturnNewTokens() throws Exception {
            RefreshToken oldToken = RefreshToken.builder()
                    .token("test-refresh-token")
                    .user(testUser)
                    .expiryDate(java.time.Instant.now().plusSeconds(3600))
                    .build();
            refreshTokenRepository.save(oldToken);

            RefreshTokenRequest request = new RefreshTokenRequest("test-refresh-token");

            MvcResult result = mockMvc.perform(post(ApiConstants.Auth.BASE + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andReturn();

            assertThat(refreshTokenRepository.findByToken("test-refresh-token")).isEmpty();

            String responseBody = result.getResponse().getContentAsString();
            LoginResponse response = objectMapper.readValue(responseBody, LoginResponse.class);
            assertThat(refreshTokenRepository.findByToken(response.getRefreshToken())).isPresent();
        }

        @Test
        @DisplayName("with expired token should return 401")
        void withExpiredToken_ShouldReturn401() throws Exception {
            RefreshToken expiredToken = RefreshToken.builder()
                    .token("expired-token")
                    .user(testUser)
                    .expiryDate(java.time.Instant.now().minusSeconds(3600))
                    .build();
            refreshTokenRepository.save(expiredToken);

            RefreshTokenRequest request = new RefreshTokenRequest("expired-token");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("with invalid token should return 401")
        void withInvalidToken_ShouldReturn401() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("non-existent-token");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Transactional
        @DisplayName("cannot reuse refresh token after rotation")
        void cannotReuseTokenAfterRotation() throws Exception {
            RefreshToken token = RefreshToken.builder()
                    .token("test-token")
                    .user(testUser)
                    .expiryDate(java.time.Instant.now().plusSeconds(3600))
                    .build();
            refreshTokenRepository.save(token);

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RefreshTokenRequest("test-token"))))
                    .andExpect(status().isOk());

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RefreshTokenRequest("test-token"))))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("multiple tokens for same user should all be valid (multi-device support)")
        void multipleTokensForSameUser_ShouldAllBeValid() throws Exception {
            RefreshToken token1 = RefreshToken.builder()
                    .token("device1-token")
                    .user(testUser)
                    .expiryDate(java.time.Instant.now().plusSeconds(3600))
                    .build();

            RefreshToken token2 = RefreshToken.builder()
                    .token("device2-token")
                    .user(testUser)
                    .expiryDate(java.time.Instant.now().plusSeconds(3600))
                    .build();

            refreshTokenRepository.save(token1);
            refreshTokenRepository.save(token2);

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RefreshTokenRequest("device1-token"))))
                    .andExpect(status().isOk());

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RefreshTokenRequest("device2-token"))))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Logout")
    class LogoutTests {

        @Test
        @DisplayName("with valid token should delete refresh token")
        void withValidToken_ShouldDeleteRefreshToken() throws Exception {
            RefreshToken token = RefreshToken.builder()
                    .token("logout-test-token")
                    .user(testUser)
                    .expiryDate(java.time.Instant.now().plusSeconds(3600))
                    .build();
            refreshTokenRepository.save(token);

            RefreshTokenRequest request = new RefreshTokenRequest("logout-test-token");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));

            assertThat(refreshTokenRepository.findByToken("logout-test-token")).isEmpty();
        }

        @Test
        @DisplayName("with non-existent token should still succeed (idempotent)")
        void withNonExistentToken_ShouldStillSucceed() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("non-existent-token");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }
    }

    @Nested
    @DisplayName("Content Type and Request Validation")
    class ContentTypeAndRequestValidationTests {

        @Test
        @DisplayName("without Content-Type header should return 415")
        void withoutContentType_ShouldReturn415() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "password");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.status").value(415))
                    .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                    .andExpect(jsonPath("$.message").value("Content-Type 'application/json' is required"));
        }

        @Test
        @DisplayName("with wrong Content-Type should return 415")
        void withWrongContentType_ShouldReturn415() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "password");

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_XML)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("with malformed JSON should return 400")
        void withMalformedJson_ShouldReturn400() throws Exception {
            String malformedJson = "{username: testuser, password: }";

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("JSON")));
        }
    }

    @Nested
    @DisplayName("Performance")
    class PerformanceTests {

        @Test
        @DisplayName("login should respond within reasonable time")
        void login_ShouldRespondWithinReasonableTime() throws Exception {
            LoginRequest request = new LoginRequest("testuser", "password");

            long startTime = System.currentTimeMillis();

            mockMvc.perform(post(ApiConstants.Auth.BASE + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            long duration = System.currentTimeMillis() - startTime;

            assertThat(duration).isLessThan(2000L);
        }
    }
}