package com.ipproxy.overseas.customer.entity.auth;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterRequest {
    @NotBlank(message = "email不能为空")
    @Email(message = "email格式不正确")
    private String email;
    @NotBlank(message = "password不能为空")
    private String password;
    @NotBlank(message = "code不能为空")
    private String code;
    private String telegram;
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
