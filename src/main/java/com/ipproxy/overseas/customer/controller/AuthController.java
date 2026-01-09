package com.ipproxy.overseas.customer.controller;

import com.ipproxy.overseas.customer.common.ApiResponse;
import com.ipproxy.overseas.customer.entity.Account;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.entity.auth.LoginRequest;
import com.ipproxy.overseas.customer.entity.auth.LoginResponse;
import com.ipproxy.overseas.customer.entity.auth.MeResponse;
import com.ipproxy.overseas.customer.entity.auth.RefreshRequest;
import com.ipproxy.overseas.customer.entity.auth.RefreshResponse;
import com.ipproxy.overseas.customer.entity.auth.RegisterRequest;
import com.ipproxy.overseas.customer.entity.auth.RegisterResponse;
import com.ipproxy.overseas.customer.security.InMemoryTokenStore;
import com.ipproxy.overseas.customer.security.JwtTokenService;
import com.ipproxy.overseas.customer.security.JwtUser;
import com.ipproxy.overseas.customer.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Instant;

@RestController
public class AuthController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private InMemoryTokenStore tokenStore;

    @PostMapping("/auth/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        Account account = accountService.register(request.getEmail(), request.getPassword());
        String token = jwtTokenService.createAccessToken(String.valueOf(account.getUid()), account.getEmail());
        return ApiResponse.success(new RegisterResponse(String.valueOf(account.getUid()), account.getEmail(), token));
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Account account = accountService.authenticate(request.getEmail(), request.getPassword());
        String uid = String.valueOf(account.getUid());
        JwtTokenService.TokenPair pair = jwtTokenService.issueTokenPair(uid, account.getEmail());
        tokenStore.storeRefreshToken(pair.getRefreshJti(), uid, pair.getRefreshExpiresAt());
        return ApiResponse.success(new LoginResponse(uid, account.getEmail(), pair.getAccessToken(), pair.getRefreshToken()));
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        JwtTokenService.VerifiedRefreshToken verified = jwtTokenService.verifyRefreshToken(request.getRefreshToken());
        InMemoryTokenStore.RefreshTokenRecord record = tokenStore.findRefreshToken(verified.getJti()).orElseThrow(() -> new UnauthorizedException("未授权"));
        if (record.isRevoked()) {
            throw new UnauthorizedException("未授权");
        }
        if (record.getExpiresAt() != null && record.getExpiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("未授权");
        }
        tokenStore.revokeRefreshToken(verified.getJti());
        JwtTokenService.TokenPair pair = jwtTokenService.issueTokenPair(verified.getUserId(), verified.getEmail());
        tokenStore.storeRefreshToken(pair.getRefreshJti(), verified.getUserId(), pair.getRefreshExpiresAt());
        return ApiResponse.success(new RefreshResponse(pair.getAccessToken(), pair.getRefreshToken()));
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("未授权");
        }
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        tokenStore.revokeAllRefreshTokensForUser(jwtUser.getUserId());

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length()).trim();
            if (!token.isEmpty()) {
                try {
                    JwtTokenService.VerifiedToken verified = jwtTokenService.verifyAccessToken(token);
                    tokenStore.revokeAccessToken(verified.getJti());
                } catch (Exception ignored) {
                }
            }
        }
        return ApiResponse.successMessage("退出成功");
    }

    @GetMapping("/me")
    public ApiResponse<MeResponse> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("未授权");
        }
        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        return ApiResponse.success(new MeResponse(jwtUser.getUserId(), jwtUser.getEmail()));
    }
}
