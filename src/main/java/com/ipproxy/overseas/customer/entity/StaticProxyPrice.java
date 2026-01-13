package com.ipproxy.overseas.customer.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StaticProxyPrice {
    private Long id;
    private Long uid;           // 用户ID
    private String type;        // 代理类型
    private String quality;     // 质量
    private String area;        // 区域
    private String region;      // 地区
    private BigDecimal price;   // 价格
    private BigDecimal discountPrice; // 折扣价格
}