package com.ipproxy.overseas.customer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new ComplexMd5PasswordEncoder();
    }

    public static class ComplexMd5PasswordEncoder implements PasswordEncoder {
        // 固定盐值，增加复杂度
        private static final String SALT = "ip_proxy_overseas_customer_salt_2026";

        @Override
        public String encode(CharSequence rawPassword) {
            try {
                // 逻辑：SHA256(password + salt) -> MD5
                String input = rawPassword + SALT;
                // SHA-256
                MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
                byte[] sha256Bytes = sha256Digest.digest(input.getBytes(StandardCharsets.UTF_8));
                String sha256Hex = bytesToHex(sha256Bytes);
                // MD5
                return DigestUtils.md5DigestAsHex(sha256Hex.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("加密算法不可用", e);
            }
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            if (rawPassword == null || encodedPassword == null) {
                return false;
            }
            return encodedPassword.equals(encode(rawPassword));
        }

        private String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        }
    }
}
