package com.chronicdisease.record.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 用户信息VO（Feign远程调用）
 */
@Data
public class UserInfoVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String realName;
    private String avatar;
    private String phone;
    private String roleType;
}
