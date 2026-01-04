package com.gderuki.taskr.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "mysecretkeymustbeverylongformacshaalgorithm";
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        Long expiration = 3_600_000L;
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        userDetails = new User("testuser", "password", Collections.emptyList());
    }

    @Test
    void generateToken_ShouldReturnToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(userDetails);
        String username = jwtUtil.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void extractExpiration_ShouldReturnDateInFuture() {
        String token = jwtUtil.generateToken(userDetails);
        Date expirationDate = jwtUtil.extractExpiration(token);
        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(userDetails);
        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithInvalidUsername_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails otherUser = new User("otheruser", "password", Collections.emptyList());
        boolean isValid = jwtUtil.validateToken(token, otherUser);
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        JwtUtil shortLivedJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwtUtil, "secret", secret);
        ReflectionTestUtils.setField(shortLivedJwtUtil, "expiration", -1000L);
        
        String token = shortLivedJwtUtil.generateToken(userDetails);
        
        boolean isValid = jwtUtil.validateToken(token, userDetails);
        assertThat(isValid).isFalse();
    }
}
