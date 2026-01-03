package com.gderuki.taskr.repository;

import com.gderuki.taskr.base.WithTestContainer;
import com.gderuki.taskr.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTest extends WithTestContainer {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    class FindByUsernameTests {

        @Test
        void shouldReturnUserWhenExists() {
            User user = User.builder()
                    .username("repo-test-user")
                    .email("repo-test@example.com")
                    .password("password")
                    .build();
            userRepository.save(user);

            Optional<User> found = userRepository.findByUsername("repo-test-user");
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("repo-test@example.com");
        }

        @Test
        void shouldReturnEmptyWhenUserDoesNotExist() {
            Optional<User> found = userRepository.findByUsername("non-existent-user");
            assertThat(found).isEmpty();
        }

        @Test
        void shouldBeCaseInsensitive() {
            User user = User.builder()
                    .username("CaseSensitiveTestUser")
                    .email("case-sensitive-test@example.com")
                    .password("password")
                    .build();
            userRepository.save(user);

            Optional<User> foundMixedCase = userRepository.findByUsername("CaseSensitiveTestUser");
            assertThat(foundMixedCase).isPresent();
        }
    }

    @Nested
    class FindByEmailTests {

        @Test
        void shouldReturnUserWhenExists() {
            User user = User.builder()
                    .username("repo-test-user-2")
                    .email("repo-test-2@example.com")
                    .password("password")
                    .build();
            userRepository.save(user);

            Optional<User> found = userRepository.findByEmail("repo-test-2@example.com");
            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("repo-test-user-2");
        }

        @Test
        void shouldReturnEmptyWhenUserDoesNotExist() {
            Optional<User> found = userRepository.findByEmail("non-existent@example.com");
            assertThat(found).isEmpty();
        }
    }

    @Nested
    class ExistsByUsernameTests {

        @Test
        void shouldWork() {
            User user = User.builder()
                    .username("repo-test-user-3")
                    .email("repo-test-3@example.com")
                    .password("password")
                    .build();
            userRepository.save(user);

            assertThat(userRepository.existsByUsername("repo-test-user-3")).isTrue();
            assertThat(userRepository.existsByUsername("other")).isFalse();
        }
    }

    @Nested
    class ExistsByEmailTests {

        @Test
        void shouldWork() {
            User user = User.builder()
                    .username("repo-test-user-4")
                    .email("repo-test-4@example.com")
                    .password("password")
                    .build();
            userRepository.save(user);

            assertThat(userRepository.existsByEmail("repo-test-4@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("other@example.com")).isFalse();
        }
    }

    @Nested
    class SaveUserTests {

        @Test
        void shouldThrowExceptionWithDuplicateUsername() {
            User user1 = User.builder()
                    .username("duplicate-user")
                    .email("user1@example.com")
                    .password("password")
                    .build();
            userRepository.save(user1);
            userRepository.flush();

            User user2 = User.builder()
                    .username("duplicate-user")
                    .email("user2@example.com")
                    .password("password")
                    .build();

            assertThatThrownBy(() ->
                    userRepository.saveAndFlush(user2)).isInstanceOf(DataIntegrityViolationException.class
            );
        }

        @Test
        void shouldThrowExceptionWithDuplicateEmail() {
            User user1 = User.builder()
                    .username("user1")
                    .email("duplicate@example.com")
                    .password("password")
                    .build();
            userRepository.save(user1);
            userRepository.flush();

            User user2 = User.builder()
                    .username("user2")
                    .email("duplicate@example.com")
                    .password("password")
                    .build();

            assertThatThrownBy(() ->
                    userRepository.saveAndFlush(user2)).isInstanceOf(DataIntegrityViolationException.class
            );
        }

        @Test
        void shouldThrowExceptionWithNullUsername() {
            User user = User.builder()
                    .username(null)
                    .email("test@example.com")
                    .password("password")
                    .build();

            assertThatThrownBy(() ->
                    userRepository.saveAndFlush(user)).isInstanceOf(DataIntegrityViolationException.class
            );
        }

        @Test
        void shouldThrowExceptionWithNullEmail() {
            User user = User.builder()
                    .username("testuser")
                    .email(null)
                    .password("password")
                    .build();

            assertThatThrownBy(() ->
                    userRepository.saveAndFlush(user)).isInstanceOf(DataIntegrityViolationException.class
            );
        }
    }

    @Nested
    class DeleteUserTests {

        @Test
        void shouldWork() {
            User user = User.builder()
                    .username("user-to-delete")
                    .email("delete@example.com")
                    .password("password")
                    .build();
            User saved = userRepository.save(user);

            userRepository.deleteById(saved.getId());

            assertThat(userRepository.findById(saved.getId())).isEmpty();
            assertThat(userRepository.findByUsername("user-to-delete")).isEmpty();
        }
    }

    @Nested
    class UpdateUserTests {

        @Test
        void shouldWork() {
            User user = User.builder()
                    .username("original-username")
                    .email("original@example.com")
                    .password("password")
                    .build();
            User saved = userRepository.save(user);

            saved.setEmail("updated@example.com");
            User updated = userRepository.save(saved);

            assertThat(updated.getEmail()).isEqualTo("updated@example.com");
            assertThat(updated.getUsername()).isEqualTo("original-username");
            assertThat(userRepository.findByEmail("updated@example.com")).isPresent();
            assertThat(userRepository.findByEmail("original@example.com")).isEmpty();
        }
    }
}
