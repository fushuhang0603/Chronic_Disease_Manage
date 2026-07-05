package com.chronicdisease.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user")
@Schema(description = "用户实体类")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "用户id")
    private Long id;

    @TableField("username")
    @Schema(description = "用户名")
    private String username;

    @JsonIgnore
    @TableField("password")
    @Schema(description = "密码")
    private String password;

    @TableField("nickname")
    @Schema(description = "昵称")
    private String nickname;

    @TableField("avatar")
    @Schema(description = "头像地址")
    private String avatar;

    @TableField("phone")
    @Schema(description = "手机号")
    private String phone;

    @TableField("role_type")
    @Schema(description = "角色类型：admin-超级管理员, doctor-医生, patient-患者")
    private String roleType;

    @TableField("status")
    @Schema(description = "状态 1-启用,0-禁用")
    private Integer status;

    @TableField("last_login_time")
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "是否删除 1-删除,0-未删除")
    private Integer isDeleted;

}
