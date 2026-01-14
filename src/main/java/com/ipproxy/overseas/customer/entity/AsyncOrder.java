package com.ipproxy.overseas.customer.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AsyncOrder {
    private Long id;
    private String orderNo;              // 返回的订单号
    private String systemOrderNo;        // 系统生成订单号
    private Long uid;                    // 用户ID
    private BigDecimal unitPrice;        // 单价
    private Integer quantity;            // 数量
    private Boolean autoRenew;           // 是否自动续费
    private String protocol;           // 协议
    private String asnType;              // ASN类型
    private String quality;              // 质量类型
    private Integer stockId;             // 库存ID
    private LocalDateTime createdAt;     // 创建时间
    private String status;               // 订单状态
}