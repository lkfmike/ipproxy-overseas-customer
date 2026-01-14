package com.ipproxy.overseas.customer.entity.proxy;

import lombok.Data;

@Data
public class PurchaseStaticProxyRequest {
    private Integer stockId;              // 库存ID
    private Integer quantity;             // 购买数量
    private Integer period;               // 使用期限（天）：30, 60, 120,360
    private String protocol;              // SOCKS5 或 VMESS
    private Boolean dedicatedLine;        // 是否选择专线中转
    private String bandwidth;             // 专线带宽：3M, 5M, 10M（仅dedicatedLine为true时需要）
    private Boolean autoRenew = false;            // 是否自动续费

}