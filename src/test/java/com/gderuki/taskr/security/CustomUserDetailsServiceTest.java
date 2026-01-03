package com.gderuki.taskr.security;

import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        User user = User.builder()
                .username("testuser")
                .password("password")
                .enabled(true)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("password");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("testuser"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
