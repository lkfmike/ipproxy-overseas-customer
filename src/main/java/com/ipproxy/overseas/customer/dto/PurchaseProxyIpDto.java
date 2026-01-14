package com.ipproxy.overseas.customer.dto;

import lombok.Data;

/**
 * 采购代理IP DTO - 与供应商API兼容的数据传输对象
 */
@Data
public class PurchaseProxyIpDto {
    private Integer areaId;              // 区域ID
    private Integer batchSize;           // 批次大小（数量）
    private Integer proxyProtocol = 2;   // 代理协议类型，默认为SOCKS5 (假设2代表SOCKS5)
    private boolean autoRenewal = false; // 是否自动续费
    private Integer months;              // 月数（使用期限）
}