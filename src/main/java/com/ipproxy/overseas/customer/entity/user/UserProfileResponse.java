package com.ipproxy.overseas.customer.entity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long uid;
    private String email;
    private String nickname;
    private String telegram;
    private String remark;
    private BigDecimal balance;
    private String inviteCode;
    private Date createdAt;
}
