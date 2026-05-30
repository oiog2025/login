package com.co.oscar.login.infrastructure.security;

import com.co.oscar.login.application.ports.output.RefreshTokenOutPort;
import com.co.oscar.login.domain.exceptions.UserException;
import com.co.oscar.login.infrastructure.persistence.jpa.refreshtoken.RefreshTokenEntity;
import com.co.oscar.login.infrastructure.persistence.jpa.refreshtoken.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenOutPort {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public String createRefreshToken(String username) {
        // Opcional: Limpiar tokens antiguos para evitar acumulación en BD
        refreshTokenRepository.deleteByUsername(username);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .username(username)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .createdAt(Instant.now())
                .revoked(false)
                .expired(false)
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    @Override
    @Transactional
    public void revokeToken(String tokenStr) {
        refreshTokenRepository.findByToken(tokenStr).ifPresent(token -> {
            if (!token.getRevoked()) {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            }
        });
    }

    @Override
    @Transactional // 💡 Nota: Lleva Transactional porque si está expirado, actualizamos su estado en BD
    public boolean isTokenValid(String tokenStr) {
        return refreshTokenRepository.findByToken(tokenStr)
                .map(token -> {
                    // Si ya está revocado o marcado como expirado internamente
                    if (token.getRevoked() || token.getExpired()) {
                        return false;
                    }

                    // Si el tiempo ya pasó, lo actualizamos en BD y marcamos inválido
                    if (token.getExpiryDate().isBefore(Instant.now())) {
                        token.setExpired(true);
                        refreshTokenRepository.save(token);
                        return false;
                    }

                    return true;
                }).orElse(false);
    }

    @Override
    public String getUsernameFromToken(String tokenStr) {
        return refreshTokenRepository.findByToken(tokenStr)
                .map(RefreshTokenEntity::getUsername)
                .orElseThrow(() -> new UserException("Refresh token no encontrado o inválido."));
    }
}