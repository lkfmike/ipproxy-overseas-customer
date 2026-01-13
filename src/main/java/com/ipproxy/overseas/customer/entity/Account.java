package com.ipproxy.overseas.customer.entity;

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
public class Account {
    private Long uid;
    private String email;
    private String password;
    private String nickname;
    private String telegram;
    private String apiToken;
    private String remark;
    private BigDecimal balance;
    private String inviteCode;
    private Long invitedBy;
    private Long parentUid;
    private Date createdAt;
}
