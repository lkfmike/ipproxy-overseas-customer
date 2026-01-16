package com.ipproxy.overseas.customer.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerifyCodeService {
    // 使用 Guava Cache 存储验证码，设置写入后1小时过期
    private final Cache<String, String> codeCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    // 使用 Guava Cache 存储验证码发送时间，用于频率控制，设置写入后60秒过期
    private final Cache<String, Instant> lastSendTimeCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();

    public void sendCode(String email, String type) {
        String cacheKey = email + ":" + type;
        // 检查是否在60秒内已经发送过验证码
        Instant lastSendTime = lastSendTimeCache.getIfPresent(cacheKey);
        if (lastSendTime != null) {
            // 如果距离上次发送不足60秒，则抛出异常
            throw new RuntimeException("验证码发送过于频繁，请稍后再试");
        }
        String code = generateCode();
        // 这里应该调用邮件服务发送验证码，现在模拟打印日志
        log.info("Sending verify code {} to email {} for type {}", code, email, type);
        codeCache.put(cacheKey, code);
        // 记录发送时间，用于频率控制
        lastSendTimeCache.put(cacheKey, Instant.now());
    }

    public boolean verifyCode(String email, String type, String code) {
        String key = email + ":" + type;
        String storedCode = codeCache.getIfPresent(key);
        if (storedCode == null) {
            throw new RuntimeException("验证码无效或已过期");
        }
        if (storedCode.equals(code)) {
            codeCache.invalidate(key); // 验证成功后删除
            return true;
        }
        throw new RuntimeException("验证码错误");
    }

    private String generateCode() {
        // 生成6位数字验证码
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
