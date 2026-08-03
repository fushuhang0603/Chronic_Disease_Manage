package com.chronicdisease.record.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 用户简要信息（用于Feign获取昵称）
 */
@Data
public class UserBriefVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String realName;
}
