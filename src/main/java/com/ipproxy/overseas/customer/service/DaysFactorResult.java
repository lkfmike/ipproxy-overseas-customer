package com.ipproxy.overseas.customer.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DaysFactorResult {
    private BigDecimal factor;      // 天数系数
    private BigDecimal discount;    // 折扣率
}