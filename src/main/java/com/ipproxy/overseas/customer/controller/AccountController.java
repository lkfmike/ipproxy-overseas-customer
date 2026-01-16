package com.ipproxy.overseas.customer.controller;

import com.ipproxy.overseas.customer.common.ApiResponse;
import com.ipproxy.overseas.customer.entity.Account;
import com.ipproxy.overseas.customer.entity.user.*;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.security.JwtUser;
import com.ipproxy.overseas.customer.service.AccountService;
import com.ipproxy.overseas.customer.service.VerifyCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/user")
public class AccountController {

    @Resource
    private AccountService accountService;

    @Resource
    private VerifyCodeService verifyCodeService;

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(Authentication authentication) {
        JwtUser jwtUser = getJwtUser(authentication);
        Account account = accountService.getAccount(Long.valueOf(jwtUser.getUserId()));
        UserProfileResponse response = UserProfileResponse.builder().uid(account.getUid()).email(account.getEmail()).nickname(account.getNickname()).telegram(account.getTelegram()).remark(account.getRemark()).balance(account.getBalance()).inviteCode(account.getInviteCode()).createdAt(account.getCreatedAt()).build();
        return ApiResponse.success(response);
    }

    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        JwtUser jwtUser = getJwtUser(authentication);
        accountService.updateProfile(Long.valueOf(jwtUser.getUserId()), request.getNickname(), request.getTelegram(), request.getRemark());
        return ApiResponse.successMessage("更新成功");
    }

    @GetMapping("/balance")
    public ApiResponse<UserBalanceResponse> getBalance(Authentication authentication) {
        JwtUser jwtUser = getJwtUser(authentication);
        Account account = accountService.getAccount(Long.valueOf(jwtUser.getUserId()));
        // 汇率换算逻辑，假设 1 USD = 7.2 CNY，实际应从配置或服务获取
        BigDecimal rate = new BigDecimal("7.2");
        BigDecimal balanceCNY = account.getBalance().multiply(rate);
        // 冻结金额暂定为0，后续如有需求可扩展
        BigDecimal frozen = BigDecimal.ZERO;
        UserBalanceResponse response = UserBalanceResponse.builder().balance(account.getBalance()).balanceCNY(balanceCNY).frozen(frozen).build();
        return ApiResponse.success(response);
    }

    @PostMapping("/send-verify-code")
    public ApiResponse<Void> sendVerifyCode(@Valid @RequestBody SendVerifyCodeRequest request) {
        try {
            verifyCodeService.sendCode(request.getEmail(), request.getType());
        } catch (RuntimeException e) {
            return ApiResponse.error(429, e.getMessage());
        }
        return ApiResponse.successMessage("验证码已发送");
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            verifyCodeService.verifyCode(request.getEmail(), "reset-password", request.getCode());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        accountService.updatePasswordByEmail(request.getEmail(), request.getPassword());
        return ApiResponse.successMessage("密码重置成功");
    }

    private JwtUser getJwtUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("未授权");
        }
        return (JwtUser) authentication.getPrincipal();
    }
}
