package com.ipproxy.overseas.customer.entity.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponse {

    private String token;
    private String refreshToken;
}
