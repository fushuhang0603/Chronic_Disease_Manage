package com.chronicdisease.user.domain.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户查询参数")
public class UserQuery {

    @Schema(description = "用户名")
    private String username;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "角色类型")
    private String roleType;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "页码")
    private Integer pageNum = 1;
    @Schema(description = "每页大小")
    private Integer pageSize = 10;

}
