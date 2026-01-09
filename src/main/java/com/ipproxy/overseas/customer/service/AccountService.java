package com.ipproxy.overseas.customer.service;

import com.ipproxy.overseas.customer.entity.Account;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account register(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (accountMapper.countByEmail(normalizedEmail) > 0) {
            throw new IllegalArgumentException("邮箱已注册");
        }
        String passwordHash = passwordEncoder.encode(password);
        Account account = Account.builder()
                .email(normalizedEmail)
                .password(passwordHash)
                .balance(BigDecimal.ZERO)
                .invitedBy(0L)
                .parentUid(0L)
                .build();
        accountMapper.insertAccount(account);
        return account;
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
