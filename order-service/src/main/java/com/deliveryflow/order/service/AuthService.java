package com.deliveryflow.order.service;

import com.deliveryflow.order.domain.RefreshToken;
import com.deliveryflow.order.domain.Role;
import com.deliveryflow.order.domain.User;
import com.deliveryflow.order.dto.AuthResponse;
import com.deliveryflow.order.dto.LoginRequest;
import com.deliveryflow.order.dto.RefreshRequest;
import com.deliveryflow.order.dto.RegisterRequest;
import com.deliveryflow.order.exception.EmailAlreadyExistsException;
import com.deliveryflow.order.exception.InvalidCredentialsException;
import com.deliveryflow.order.exception.InvalidRefreshTokenException;
import com.deliveryflow.order.repository.RefreshTokenRepository;
import com.deliveryflow.order.repository.UserRepository;
import com.deliveryflow.order.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                        RefreshTokenRepository refreshTokenRepository,
                        JwtTokenProvider jwtTokenProvider,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        log.info("Registered user userId={}", user.getUserId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        log.info("User logged in userId={}", user.getUserId());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException();
        }
        stored.setRevoked(true);

        User user = userRepository.findByUserId(stored.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        log.info("Refreshed tokens for userId={}", user.getUserId());
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    log.info("Logged out userId={}", token.getUserId());
                });
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        RefreshToken entity = new RefreshToken();
        entity.setTokenId(UUID.randomUUID());
        entity.setUserId(user.getUserId());
        entity.setTokenHash(hash(refreshToken));
        entity.setExpiresAt(Instant.now().plusSeconds(jwtTokenProvider.getRefreshExpirationSeconds()));
        entity.setRevoked(false);
        entity.setCreatedAt(Instant.now());
        refreshTokenRepository.save(entity);

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtTokenProvider.getAccessExpirationSeconds());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
