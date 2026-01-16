package com.ipproxy.overseas.customer.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        private static final Logger LOGGER = LoggerFactory.getLogger(ComplexMd5PasswordEncoder.class);
        private static final String SALT = "ip_proxy_overseas_customer_salt_2024";

        @Override
        public String encode(CharSequence rawPassword) {
            try {
                String input = rawPassword + SALT;
                MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
                byte[] sha256Bytes = sha256Digest.digest(input.getBytes(StandardCharsets.UTF_8));
                String sha256Hex = bytesToHex(sha256Bytes);
                return DigestUtils.md5DigestAsHex(sha256Hex.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error("Password encoding failed due to missing algorithm: {}", e.getMessage());
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
