package com.gderuki.taskr.service;

import com.gderuki.taskr.entity.RefreshToken;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.TokenExpiredException;
import com.gderuki.taskr.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        Long refreshTokenDurationMs = 3_600_000L;
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", refreshTokenDurationMs);
        user = User.builder().id(1L).username("testuser").build();
    }

    @Test
    void createRefreshToken_ShouldReturnSavedToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiryDate()).isAfter(Instant.now());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void findByToken_ShouldReturnOptional() {
        String token = "test-token";
        RefreshToken refreshToken = RefreshToken.builder().token(token).build();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(token);
    }

    @Test
    void verifyExpiration_WhenNotExpired_ShouldReturnToken() {
        RefreshToken refreshToken = RefreshToken.builder()
                .expiryDate(Instant.now().plusSeconds(60))
                .build();

        RefreshToken result = refreshTokenService.verifyExpiration(refreshToken);

        assertThat(result).isEqualTo(refreshToken);
    }

    @Test
    void verifyExpiration_WhenExpired_ShouldDeleteAndThrowException() {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiryDate(Instant.now().minusSeconds(60))
                .build();

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(refreshToken))
                .isInstanceOf(TokenExpiredException.class);

        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    void rotateRefreshToken_ShouldDeleteOldAndCreateNew() {
        RefreshToken oldToken = RefreshToken.builder().user(user).token("old").build();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.rotateRefreshToken(oldToken);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotEqualTo("old");
        verify(refreshTokenRepository).delete(oldToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void deleteByUser_ShouldCallRepository() {
        refreshTokenService.deleteByUser(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void deleteByToken_ShouldCallRepository() {
        String token = "test-token";
        refreshTokenService.deleteByToken(token);
        verify(refreshTokenRepository).deleteByToken(token);
    }
}
