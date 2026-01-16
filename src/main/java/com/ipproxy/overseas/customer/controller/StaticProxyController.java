package com.ipproxy.overseas.customer.controller;

import com.ipproxy.overseas.customer.common.ApiResponse;
import com.ipproxy.overseas.customer.entity.proxy.PurchaseStaticProxyRequest;
import com.ipproxy.overseas.customer.entity.proxy.PurchaseStaticProxyResponse;
import com.ipproxy.overseas.customer.exception.PurchaseException;
import com.ipproxy.overseas.customer.security.JwtUser;
import com.ipproxy.overseas.customer.service.StaticProxyPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/proxy")
public class StaticProxyController extends BaseController {

    @Resource
    private StaticProxyPurchaseService staticProxyPurchaseService;

    /**
     * 购买静态代理
     *
     * @param authentication 认证信息
     * @param request        购买请求参数
     * @return 购买结果
     */
    @PostMapping("/static/purchase")
    public ApiResponse<PurchaseStaticProxyResponse> purchaseStaticProxy(Authentication authentication, @RequestBody PurchaseStaticProxyRequest request) {
        try {
            JwtUser jwtUser = getJwtUser(authentication);
            Long userId = Long.valueOf(jwtUser.getUserId());
            PurchaseStaticProxyResponse response = staticProxyPurchaseService.purchaseStaticProxy(userId, request);
            return ApiResponse.success(response);
        } catch (PurchaseException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
