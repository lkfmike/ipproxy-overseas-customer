package com.ipproxy.overseas.customer.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class VerifyCodeService {

    // 使用 Guava Cache 存储验证码，设置写入后5分钟过期
    private final Cache<String, String> codeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public void sendCode(String email, String type) {
        String code = generateCode();
        // 这里应该调用邮件服务发送验证码，现在模拟打印日志
        log.info("Sending verify code {} to email {} for type {}", code, email, type);
        codeCache.put(email + ":" + type, code);
    }

    public boolean verifyCode(String email, String type, String code) {
        String key = email + ":" + type;
        String storedCode = codeCache.getIfPresent(key);
        if (storedCode == null) {
            return false;
        }
        if (storedCode.equals(code)) {
            codeCache.invalidate(key); // 验证成功后删除
            return true;
        }
        return false;
    }

    private String generateCode() {
        // 生成6位数字验证码
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
