package com.gderuki.taskr.controller;

import com.gderuki.taskr.config.ApiConstants;
import com.gderuki.taskr.dto.LoginRequest;
import com.gderuki.taskr.dto.LoginResponse;
import com.gderuki.taskr.dto.MessageResponse;
import com.gderuki.taskr.dto.RefreshTokenRequest;
import com.gderuki.taskr.entity.RefreshToken;
import com.gderuki.taskr.entity.User;
import com.gderuki.taskr.exception.RefreshTokenException;
import com.gderuki.taskr.repository.UserRepository;
import com.gderuki.taskr.security.CustomUserDetailsService;
import com.gderuki.taskr.security.JwtUtil;
import com.gderuki.taskr.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.AUTH_PATH)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @NonNull @RequestBody LoginRequest loginRequest) {
        log.debug("Login attempt for user: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails == null) {
            log.error("Authentication principal is null for user: {}", loginRequest.getUsername());
            throw new IllegalStateException("Authentication failed: user details not found");
        }

        String jwt = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found after successful authentication"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in successfully: {}", user.getUsername());

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @NonNull @RequestBody RefreshTokenRequest request) {
        log.debug("Refresh token request received");

        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(oldToken -> {
                    RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldToken);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(newRefreshToken.getUser().getUsername());
                    String token = jwtUtil.generateToken(userDetails);

                    log.info("Access token and refresh token rotated for user: {}", newRefreshToken.getUser().getUsername());

                    return ResponseEntity.ok(LoginResponse.builder()
                            .accessToken(token)
                            .refreshToken(newRefreshToken.getToken())
                            .tokenType("Bearer")
                            .expiresIn(jwtExpiration / 1000)
                            .build());
                })
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @NonNull @RequestBody RefreshTokenRequest request) {
        log.debug("Logout request received");

        refreshTokenService.deleteByToken(request.getRefreshToken());

        log.info("User logged out successfully");

        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
