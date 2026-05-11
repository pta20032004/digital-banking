package com.tony.demo.modules.auth.application;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tony.demo.core.security.JwtService;
import com.tony.demo.modules.auth.domain.RefreshToken;
import com.tony.demo.modules.auth.domain.RefreshTokenRepository;
import com.tony.demo.modules.user.domain.User;
import com.tony.demo.modules.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    @Transactional
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Delete existing refresh tokens for user (optional, depending on allowed active sessions)
        refreshTokenRepository.deleteByUser(user);

        // Save new refresh token
        RefreshToken rToken = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .isRevoked(false)
                .createdAt(Instant.now())
                .build();
        refreshTokenRepository.save(rToken);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);
        
        // Add access token to Redis blacklist
        redisTemplate.opsForValue().set("blacklist:token:" + jwt, "true", accessExpiration, TimeUnit.MILLISECONDS);

        // Remove refresh token from DB
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            refreshTokenRepository.deleteByUser(user);
        }
    }
}
