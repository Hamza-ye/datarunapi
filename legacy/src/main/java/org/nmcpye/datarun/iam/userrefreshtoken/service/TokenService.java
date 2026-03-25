package org.nmcpye.datarun.iam.userrefreshtoken.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.user.repository.UserRepository;
import org.nmcpye.datarun.iam.userauthority.Authority;
import org.nmcpye.datarun.iam.userrefreshtoken.RefreshToken;
import org.nmcpye.datarun.iam.userrefreshtoken.TokenRefreshException;
import org.nmcpye.datarun.iam.userrefreshtoken.dto.RefreshTokenDto;
import org.nmcpye.datarun.iam.userrefreshtoken.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.nmcpye.datarun.iam.security.SecurityUtils.AUTHORITIES_KEY;

/**
 * @author Hamza Assada 16/04/2025 (7amza.it@gmail.com)
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;

    @Value("${datarun.security.authentication.jwt.token-validity-in-seconds:600}")
    private long accessTokenValidity;

    @Value("${datarun.security.authentication.jwt.refresh-token-validity-in-seconds:2592000}") // 30 days
    private long refreshTokenValidity;

    @Value("${datarun.security.authentication.jwt.rsa-key-id:datarun-rs256-1}")
    private String rsaKeyId;

    public Optional<RefreshTokenDto> findDtoByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Transactional(readOnly = true)
    public String generateAccessToken(String username) {
        Instant now = Instant.now();
        final var user = userRepository
            .findOneWithAuthoritiesByLogin(username)
            .orElseThrow(() -> new TokenRefreshException(username, "invalid"));
        String authorities = user.getAuthorities().stream()
            .map(Authority::getName)
            .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plus(accessTokenValidity, ChronoUnit.SECONDS))
            .subject(user.getLogin())
            .claim(AUTHORITIES_KEY, authorities)
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public RefreshTokenDto createRefreshToken(String username) {
        final User user = userRepository
            .findOneWithAuthoritiesByLogin(username)
            .orElseThrow(() -> new TokenRefreshException(username, "invalid"));
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenValidity, ChronoUnit.SECONDS));
        refreshToken.setToken(UUID.randomUUID().toString());

        return Optional.of(tokenRepository.save(refreshToken))
            .map(RefreshTokenDto::new).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Optional<RefreshTokenDto> verifyRefreshToken(String token) {
        return tokenRepository.findByToken(token)
            .filter(t -> !t.isExpired());
    }

    /**
     * Invalidate the current refresh token and issue a new one.
     */
    public RefreshTokenDto rotateRefreshToken(RefreshTokenDto refreshToken) {
        tokenRepository.deleteByUserUid(refreshToken.getUser().getUid());
        return createRefreshToken(refreshToken.getUser().getLogin());
    }

    /**
     * Delete expired tokens, runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokens() {
        tokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }
}
