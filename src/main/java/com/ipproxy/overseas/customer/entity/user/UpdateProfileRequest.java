package com.ipproxy.overseas.customer.entity.user;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nickname;
    private String telegram;
    private String remark;
}
