package com.ipproxy.overseas.customer.security;

import lombok.Getter;

@Getter
public class JwtUser {

    private final String userId;
    private final String email;

    public JwtUser(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }
}
