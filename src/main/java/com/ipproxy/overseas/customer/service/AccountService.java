package com.ipproxy.overseas.customer.service;

import com.ipproxy.overseas.customer.entity.Account;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account register(String email, String password, String inviteCode) {
        String normalizedEmail = normalizeEmail(email);
        if (accountMapper.countByEmail(normalizedEmail) > 0) {
            throw new IllegalArgumentException("邮箱已注册");
        }
        Long invitedBy = 0L;
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            Account inviter = accountMapper.selectByInviteCode(inviteCode.trim());
            if (inviter != null) {
                invitedBy = inviter.getUid();
            }
        }
        String myInviteCode = generateUniqueInviteCode();
        String passwordHash = passwordEncoder.encode(password);
        Account account = Account.builder()
                .email(normalizedEmail)
                .password(passwordHash)
                .balance(BigDecimal.ZERO)
                .invitedBy(invitedBy)
                .parentUid(0L)
                .inviteCode(myInviteCode)
                .build();
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
        throw new RuntimeException("生成邀请码失败，请重试");
    }

    public Account authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        Account account = accountMapper.selectByEmail(normalizedEmail);
        if (account == null) {
            throw new UnauthorizedException("账号或密码错误");
        }
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new UnauthorizedException("账号或密码错误");
        }
        return account;
    }

    public Account getAccount(Long uid) {
        return accountMapper.selectByUid(uid);
    }

    public void updateProfile(Long uid, String nickname, String telegram, String remark) {
        Account account = Account.builder()
                .uid(uid)
                .nickname(nickname)
                .telegram(telegram)
                .remark(remark)
                .build();
        accountMapper.updateAccount(account);
    }

    public void updatePassword(Long uid, String newPassword) {
        String passwordHash = passwordEncoder.encode(newPassword);
        Account account = Account.builder()
                .uid(uid)
                .password(passwordHash)
                .build();
        accountMapper.updateAccount(account);
    }

    public void updatePasswordByEmail(String email, String newPassword) {
        Account account = accountMapper.selectByEmail(email);
        if (account == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        updatePassword(account.getUid(), newPassword);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
