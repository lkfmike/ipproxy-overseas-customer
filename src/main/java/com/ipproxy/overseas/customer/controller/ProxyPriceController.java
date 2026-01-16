package com.ipproxy.overseas.customer.controller;

import com.ipproxy.overseas.customer.common.ApiResponse;
import com.ipproxy.overseas.customer.entity.StaticProxyPrice;
import com.ipproxy.overseas.customer.exception.UnauthorizedException;
import com.ipproxy.overseas.customer.security.JwtUser;
import com.ipproxy.overseas.customer.service.StaticProxyPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/proxy")
public class ProxyPriceController {

    @Resource
    private StaticProxyPriceService staticProxyPriceService;

    /**
     * 获取用户的代理价格
     *
     * @param authentication 认证信息
     * @param type           代理类型
     * @return 用户的代理价格
     */
    @GetMapping("/proxy-price")
    public ApiResponse<BigDecimal> getProxyPrice(Authentication authentication, @RequestParam String type) {
        JwtUser jwtUser = getJwtUser(authentication);
        Long uid = Long.valueOf(jwtUser.getUserId());

        BigDecimal price = staticProxyPriceService.getProxyPrice(uid, type);

        return ApiResponse.success(price);
    }

    @GetMapping("/proxy-price-list")
    public ApiResponse<List<StaticProxyPrice>> getProxyPriceList(Authentication authentication) {
        JwtUser jwtUser = getJwtUser(authentication);
        Long uid = Long.valueOf(jwtUser.getUserId());
        List<StaticProxyPrice> priceList = staticProxyPriceService.getProxyPriceListByUid(uid);
        return ApiResponse.success(priceList);
    }

    private JwtUser getJwtUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("未授权");
        }
        return (JwtUser) authentication.getPrincipal();
    }
}