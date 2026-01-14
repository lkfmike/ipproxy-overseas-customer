package com.ipproxy.overseas.customer.entity.proxy;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseStaticProxyResponse {
    private String orderId;               // 订单ID
    private BigDecimal totalAmount;       // 总金额（美元）
    private Detail detail;                // 详细价格信息

    @Data
    public static class Detail {
        private BigDecimal basePrice;     // IP基础价格
        private BigDecimal bandwidthPrice;// 专线带宽费用
        private BigDecimal discount;      // 折扣金额
    }
}