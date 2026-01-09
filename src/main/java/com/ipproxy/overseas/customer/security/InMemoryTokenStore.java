package com.ipproxy.overseas.customer.security;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryTokenStore {

    private final Map<String, RefreshTokenRecord> refreshTokensByJti = new ConcurrentHashMap<>();
    private final Set<String> revokedAccessJti = ConcurrentHashMap.newKeySet();

    public void storeRefreshToken(String jti, String userId, Instant expiresAt) {
        refreshTokensByJti.put(jti, new RefreshTokenRecord(jti, userId, expiresAt, false));
    }

    public Optional<RefreshTokenRecord> findRefreshToken(String jti) {
        return Optional.ofNullable(refreshTokensByJti.get(jti));
    }

    public void revokeRefreshToken(String jti) {
        RefreshTokenRecord record = refreshTokensByJti.get(jti);
        if (record != null) {
            refreshTokensByJti.put(jti, record.revoked());
        }
    }

    public void revokeAllRefreshTokensForUser(String userId) {
        refreshTokensByJti.forEach((k, v) -> {
            if (v != null && userId.equals(v.getUserId())) {
                refreshTokensByJti.put(k, v.revoked());
            }
        });
    }

    public void revokeAccessToken(String jti) {
        revokedAccessJti.add(jti);
    }

    public boolean isAccessTokenRevoked(String jti) {
        return revokedAccessJti.contains(jti);
    }

    @Getter
    public static class RefreshTokenRecord {

        private final String jti;
        private final String userId;
        private final Instant expiresAt;
        private final boolean revoked;

        public RefreshTokenRecord(String jti, String userId, Instant expiresAt, boolean revoked) {
            this.jti = jti;
            this.userId = userId;
            this.expiresAt = expiresAt;
            this.revoked = revoked;
        }

        public RefreshTokenRecord revoked() {
            return new RefreshTokenRecord(jti, userId, expiresAt, true);
        }
    }
}
