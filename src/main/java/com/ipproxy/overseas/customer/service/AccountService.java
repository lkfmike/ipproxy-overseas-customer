package com.ipproxy.overseas.customer.service;

import com.ipproxy.overseas.customer.common.Constants;
import com.ipproxy.overseas.customer.entity.Account;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.mapper.AccountMapper;
import com.ipproxy.overseas.customer.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerifyCodeService verifyCodeService;

    public Account register(String email, String password, String code, String telegram, String inviteCode) {
        String normalizedEmail = normalizeEmail(email);
        if (accountMapper.countByEmail(normalizedEmail) > 0) {
            log.warn("Register failed: email {} already exists", normalizedEmail);
            throw new IllegalArgumentException("邮箱已注册");
        }
        Long invitedBy = 0L;
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            Account inviter = accountMapper.selectByInviteCode(inviteCode.trim());
            if (inviter == null) {
                log.warn("Register failed: invalid invite code {}", inviteCode);
                throw new IllegalArgumentException("邀请码无效");
            }
            invitedBy = inviter.getUid();
        }
        // 验证注册验证码
        try {
            verifyCodeService.verifyCode(normalizedEmail, Constants.VerifyCodeType.REGISTER, code);
        } catch (RuntimeException e) {
            log.warn("Register failed: verification code error for email {}: {}", normalizedEmail, e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        String myInviteCode = generateUniqueInviteCode();
        String passwordHash = passwordEncoder.encode(password);
        String apiToken = CommonUtils.generateApiToken();
        Account account = Account.builder().email(normalizedEmail).password(passwordHash).apiToken(apiToken).telegram(telegram).balance(BigDecimal.ZERO).invitedBy(invitedBy).parentUid(0L).inviteCode(myInviteCode).build();
        accountMapper.insertAccount(account);
        return account;
    }

    private String generateUniqueInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除 I, 1, O, 0 等易混淆字符
        int length = 6;
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < length; j++) {
                int index = ThreadLocalRandom.current().nextInt(chars.length());
                sb.append(chars.charAt(index));
            }
            String code = sb.toString();
            if (accountMapper.selectByInviteCode(code) == null) {
                return code;
            }
        }
        log.error("Failed to generate unique invite code after {} attempts", maxAttempts);
        throw new RuntimeException("生成邀请码失败，请重试");
    }

    public Account authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        Account account = accountMapper.selectByEmail(normalizedEmail);
        if (account == null) {
            log.warn("Authentication failed: email {} not found", normalizedEmail);
            throw new UnauthorizedException("账号或密码错误");
        }
        if (!passwordEncoder.matches(password, account.getPassword())) {
            log.warn("Authentication failed: password mismatch for email {}", normalizedEmail);
            throw new UnauthorizedException("账号或密码错误");
        }
        return account;
    }

    public Account getAccount(Long uid) {
        return accountMapper.selectByUid(uid);
    }

    public void updateProfile(Long uid, String nickname, String telegram, String remark) {
        Account account = Account.builder().uid(uid).nickname(nickname).telegram(telegram).remark(remark).build();
        accountMapper.updateAccount(account);
    }

    public void updatePassword(Long uid, String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        Account account = Account.builder().uid(uid).password(passwordHash).build();
        accountMapper.updateAccount(account);
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        Account account = accountMapper.selectByEmail(email);
        if (account == null) {
            log.warn("Update password failed: email {} not found", email);
            throw new IllegalArgumentException("用户不存在");
        }
        updatePassword(account.getUid(), newPassword);
    }

    public Account getAccountByEmail(String email) {
        return accountMapper.selectByEmail(email);
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
