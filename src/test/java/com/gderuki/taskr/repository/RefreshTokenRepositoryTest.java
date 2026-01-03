package com.gderuki.taskr.repository;

import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.entity.RefreshToken;
import com.gderuki.taskr.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenRepositoryTest extends WithTestContainer {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        user = User.builder()
                .username("refresh-repo-user")
                .email("refresh-repo@example.com")
                .password("password")
                .build();
        userRepository.save(user);
    }

    @Nested
    class FindByTokenTests {

        @Test
        void shouldReturnTokenWhenExists() {
            RefreshToken token = RefreshToken.builder()
                    .token("test-token")
                    .user(user)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();
            refreshTokenRepository.save(token);

            Optional<RefreshToken> found = refreshTokenRepository.findByToken("test-token");

            assertThat(found).isPresent();
            assertThat(found.get().getUser().getUsername()).isEqualTo("refresh-repo-user");
        }

        @Test
        void shouldReturnEmptyWhenTokenDoesNotExist() {
            Optional<RefreshToken> found = refreshTokenRepository.findByToken("non-existent-token");

            assertThat(found).isEmpty();
        }

        @Test
        void shouldStillReturnExpiredToken() {
            RefreshToken expiredToken = RefreshToken.builder()
                    .token("expired-token")
                    .user(user)
                    .expiryDate(Instant.now().minusSeconds(3600)) // 1h
                    .build();
            refreshTokenRepository.save(expiredToken);

            Optional<RefreshToken> found = refreshTokenRepository.findByToken("expired-token");

            assertThat(found).isPresent();
            assertThat(found.get().getExpiryDate()).isBefore(Instant.now());
        }
    }

    @Nested
    class DeleteByUserTests {

        @Test
        void shouldDeleteAllUserTokens() {
            RefreshToken token = RefreshToken.builder()
                    .token("test-token")
                    .user(user)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();
            refreshTokenRepository.save(token);

            refreshTokenRepository.deleteByUser(user);

            assertThat(refreshTokenRepository.findByToken("test-token")).isEmpty();
        }

        @Test
        void shouldNotThrowExceptionWhenNoTokensExist() {
            refreshTokenRepository.deleteByUser(user);
            assertThat(refreshTokenRepository.findAll()).isEmpty();
        }
    }

    @Nested
    class DeleteByTokenTests {

        @Test
        void shouldDeleteSpecificToken() {
            RefreshToken token1 = RefreshToken.builder()
                    .token("token-1")
                    .user(user)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            RefreshToken token2 = RefreshToken.builder()
                    .token("token-2")
                    .user(user)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            refreshTokenRepository.save(token1);
            refreshTokenRepository.save(token2);

            refreshTokenRepository.deleteByToken("token-1");

            assertThat(refreshTokenRepository.findByToken("token-1")).isEmpty();
            assertThat(refreshTokenRepository.findByToken("token-2")).isPresent();
        }
    }

    @Nested
    class SaveTests {

        @Test
        void shouldSaveMultipleTokensForSameUser() {
            RefreshToken token1 = RefreshToken.builder()
                    .token("token-1")
                    .user(user)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            RefreshToken token2 = RefreshToken.builder()
                    .token("token-2")
                    .user(user)
                    .expiryDate(Instant.now().plusSeconds(7200))
                    .build();

            refreshTokenRepository.save(token1);
            refreshTokenRepository.save(token2);

            assertThat(refreshTokenRepository.findByToken("token-1")).isPresent();
            assertThat(refreshTokenRepository.findByToken("token-2")).isPresent();
        }
    }
}
