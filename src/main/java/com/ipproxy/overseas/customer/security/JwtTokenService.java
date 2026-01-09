package com.ipproxy.overseas.customer.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenService {

    @Autowired
    private JwtProperties properties;

    public TokenPair issueTokenPair(String userId, String email) {
        String accessToken = createAccessToken(userId, email);
        RefreshToken refreshToken = createRefreshToken(userId, email);
        return new TokenPair(accessToken, refreshToken.getToken(), refreshToken.getJti(), refreshToken.getExpiresAt());
    }

    public String createAccessToken(String userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getExpireMinutes() * 60L);

        Algorithm algorithm = Algorithm.HMAC256(properties.getSecret());
        return JWT.create()
                .withIssuer(properties.getIssuer())
                .withSubject(email)
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .withClaim("uid", userId)
                .withClaim("tokenType", "access")
                .sign(algorithm);
    }

    public RefreshToken createRefreshToken(String userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.getRefreshExpireMinutes() * 60L);
        String jti = UUID.randomUUID().toString();
        Algorithm algorithm = Algorithm.HMAC256(properties.getSecret());
        String token = JWT.create()
                .withIssuer(properties.getIssuer())
                .withSubject(email)
                .withJWTId(jti)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiresAt))
                .withClaim("uid", userId)
                .withClaim("tokenType", "refresh")
                .sign(algorithm);
        return new RefreshToken(token, jti, expiresAt);
    }

    public VerifiedToken verifyAccessToken(String token) {
        DecodedJWT jwt = verify(token);
        String tokenType = jwt.getClaim("tokenType").asString();
        if (!"access".equals(tokenType)) {
            throw new UnauthorizedException("未授权");
        }
        String userId = jwt.getClaim("uid").asString();
        String email = jwt.getSubject();
        return new VerifiedToken(jwt.getId(), new JwtUser(userId, email));
    }

    public VerifiedRefreshToken verifyRefreshToken(String token) {
        DecodedJWT jwt = verify(token);
        String tokenType = jwt.getClaim("tokenType").asString();
        if (!"refresh".equals(tokenType)) {
            throw new UnauthorizedException("未授权");
        }
        String userId = jwt.getClaim("uid").asString();
        String email = jwt.getSubject();
        Instant expiresAt = jwt.getExpiresAt().toInstant();
        return new VerifiedRefreshToken(jwt.getId(), userId, email, expiresAt);
    }

    private DecodedJWT verify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(properties.getSecret());
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(properties.getIssuer())
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException ex) {
            throw new UnauthorizedException("未授权");
        }
    }

    public static class VerifiedToken {

        @Getter
        private final String jti;
        @Getter
        private final JwtUser user;

        public VerifiedToken(String jti, JwtUser user) {
            this.jti = jti;
            this.user = user;
        }

    }

    public static class VerifiedRefreshToken {

        @Getter
        private final String jti;
        @Getter
        private final String userId;
        @Getter
        private final String email;
        @Getter
        private final Instant expiresAt;

        public VerifiedRefreshToken(String jti, String userId, String email, Instant expiresAt) {
            this.jti = jti;
            this.userId = userId;
            this.email = email;
            this.expiresAt = expiresAt;
        }

    }

    public static class RefreshToken {

        @Getter
        private final String token;
        @Getter
        private final String jti;
        @Getter
        private final Instant expiresAt;

        public RefreshToken(String token, String jti, Instant expiresAt) {
            this.token = token;
            this.jti = jti;
            this.expiresAt = expiresAt;
        }

    }

    public static class TokenPair {

        @Getter
        private final String accessToken;
        @Getter
        private final String refreshToken;
        @Getter
        private final String refreshJti;
        @Getter
        private final Instant refreshExpiresAt;

        public TokenPair(String accessToken, String refreshToken, String refreshJti, Instant refreshExpiresAt) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.refreshJti = refreshJti;
            this.refreshExpiresAt = refreshExpiresAt;
        }

    }
}
