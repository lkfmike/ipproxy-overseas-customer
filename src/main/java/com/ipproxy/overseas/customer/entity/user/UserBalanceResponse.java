package com.ipproxy.overseas.customer.entity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceResponse {
    private BigDecimal balance;
    private BigDecimal balanceCNY;
    private BigDecimal frozen;
}
