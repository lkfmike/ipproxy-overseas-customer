package com.ipproxy.overseas.customer.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class JwtProperties {
    private String secret;
    private String issuer;
    private long expireMinutes = 120;
    private long refreshExpireMinutes = 43200;
}
