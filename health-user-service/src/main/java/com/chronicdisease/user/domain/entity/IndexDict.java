package com.chronicdisease.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("term_dict")
@Schema(description = "术语字典表实体类")
public class IndexDict implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "术语ID")
    private Long id;

    @TableField("term_code")
    @Schema(description = "术语编码")
    private String indexCode;

    @TableField("term_name")
    @Schema(description = "术语名称")
    private String indexName;

    @TableField("term_type")
    @Schema(description = "术语类型: indicator/disease/medicine")
    private String termType;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;

    @TableField("status")
    @Schema(description = "状态 1-启用,0-禁用")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @Schema(description = "是否删除 1-删除,0-未删除")
    private Integer isDeleted;

}
