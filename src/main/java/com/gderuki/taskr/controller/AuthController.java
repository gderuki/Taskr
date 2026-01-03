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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping(ApiConstants.Auth.BASE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs for login, token refresh, and logout")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Operation(
            summary = "User login",
            description = "Authenticate user with username and password. Returns JWT access token and refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                                              "tokenType": "Bearer",
                                              "expiresIn": 900
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-01-03T10:15:30.000+00:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Bad credentials",
                                              "path": "/api/v1/auth/login"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "testuser",
                                              "password": "password"
                                            }
                                            """
                            )
                    )
            )
            @Valid @NonNull @RequestBody LoginRequest loginRequest) {
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

    @Operation(
            summary = "Refresh access token",
            description = "Exchange a valid refresh token for a new access token and refresh token pair. The old refresh token will be invalidated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refresh successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "g58bd20c-69dd-5483-b678-1f13c3d4e590",
                                              "tokenType": "Bearer",
                                              "expiresIn": 900
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-01-03T10:15:30.000+00:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Refresh token expired or invalid",
                                              "path": "/api/v1/auth/refresh"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
                                            }
                                            """
                            )
                    )
            )
            @Valid @NonNull @RequestBody RefreshTokenRequest request) {
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

    @Operation(
            summary = "User logout",
            description = "Invalidate the refresh token to logout the user. The access token will remain valid until it expires."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Logged out successfully"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-01-03T10:15:30.000+00:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Refresh token is required",
                                              "path": "/api/v1/auth/logout"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token to invalidate",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
                                            }
                                            """
                            )
                    )
            )
            @Valid @NonNull @RequestBody RefreshTokenRequest request) {
        log.debug("Logout request received");

        refreshTokenService.deleteByToken(request.getRefreshToken());

        log.info("User logged out successfully");

        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }
}
