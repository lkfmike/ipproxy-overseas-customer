package com.ipproxy.overseas.customer.controller;

import com.ipproxy.overseas.customer.common.ApiResponse;
import com.ipproxy.overseas.customer.common.Constants;
import com.ipproxy.overseas.customer.entity.Account;
import com.ipproxy.overseas.customer.entity.auth.LoginRequest;
import com.ipproxy.overseas.customer.entity.auth.LoginResponse;
import com.ipproxy.overseas.customer.entity.auth.MeResponse;
import com.ipproxy.overseas.customer.entity.auth.RefreshRequest;
import com.ipproxy.overseas.customer.entity.auth.RefreshResponse;
import com.ipproxy.overseas.customer.entity.auth.RegisterRequest;
import com.ipproxy.overseas.customer.entity.auth.RegisterResponse;
import com.ipproxy.overseas.customer.entity.user.SendVerifyCodeRequest;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.security.InMemoryTokenStore;
import com.ipproxy.overseas.customer.security.JwtTokenService;
import com.ipproxy.overseas.customer.security.JwtUser;
import com.ipproxy.overseas.customer.service.AccountService;
import com.ipproxy.overseas.customer.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private InMemoryTokenStore tokenStore;
    @Autowired
    private VerifyCodeService verifyCodeService;

    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        String normalizedEmail = accountService.normalizeEmail(request.getEmail());
        boolean emailExists = accountService.getAccountByEmail(normalizedEmail) != null;
        // 根据不同类型验证邮箱存在性
        if (Constants.VerifyCodeType.REGISTER.equals(request.getType())) {
            // 注册类型：邮箱不应存在
            if (emailExists) {
                return ApiResponse.error(400, "邮箱已被注册");
            }
        } else {
            // 其他类型（如找回密码）：邮箱应存在
            if (!emailExists) {
                return ApiResponse.error(400, "邮箱账户不存在");
            }
        }
        try {
            verifyCodeService.sendCode(request.getEmail(), request.getType());
        } catch (RuntimeException e) {
            return ApiResponse.error(429, e.getMessage());
        }
        return ApiResponse.successMessage("验证码已发送");
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        Account account = accountService.register(request.getEmail(), request.getPassword(), request.getCode(), request.getTelegram(), request.getInviteCode());
        String token = jwtTokenService.createAccessToken(String.valueOf(account.getUid()), account.getEmail());
        return ApiResponse.success(new RegisterResponse(String.valueOf(account.getUid()), account.getEmail(), token));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Account account = accountService.authenticate(request.getEmail(), request.getPassword());
        String uid = String.valueOf(account.getUid());
        // 登录前先踢掉旧的登录态（单一设备登录）
        tokenStore.revokeAllRefreshTokensForUser(uid);
        JwtTokenService.TokenPair pair = jwtTokenService.issueTokenPair(uid, account.getEmail());
        tokenStore.storeRefreshToken(pair.getRefreshJti(), uid, pair.getRefreshExpiresAt());
        return ApiResponse.success(new LoginResponse(uid, account.getEmail(), pair.getAccessToken(), pair.getRefreshToken()));
    }

    @PostMapping("/refresh")
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

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("未授权");
        }
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
}
