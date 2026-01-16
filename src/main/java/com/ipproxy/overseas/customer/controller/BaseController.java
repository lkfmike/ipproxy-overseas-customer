package com.ipproxy.overseas.customer.controller;

import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.security.JwtUser;
import org.springframework.security.core.Authentication;

public class BaseController {

    /**
     * 获取当前认证用户信息
     *
     * @param authentication 认证信息
     * @return JwtUser对象
     */
    protected JwtUser getJwtUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("未授权");
        }
        return (JwtUser) authentication.getPrincipal();
    }

    /**
     * 从认证信息中获取用户ID
     *
     * @param authentication 认证信息
     * @return 用户ID
     */
    protected Long getCurrentUserId(Authentication authentication) {
        JwtUser jwtUser = getJwtUser(authentication);
        return Long.valueOf(jwtUser.getUserId());
    }
}