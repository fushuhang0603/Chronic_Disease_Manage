package com.chronicdisease.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("index_dict")
@Schema(description = "指标字典表实体类")
public class IndexDict implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "指标字典id")
    private Long id;

    @TableField("index_code")
    @Schema(description = "指标代码")
    private String indexCode;

    @TableField("index_name")
    @Schema(description = "指标名称")
    private String indexName;

    @TableField("unit")
    @Schema(description = "单位")
    private String unit;

    @TableField("normal_min")
    @Schema(description = "正常范围最小值")
    private BigDecimal normalMin;

    @TableField("normal_max")
    @Schema(description = "正常范围最大值")
    private BigDecimal normalMax;

    @TableField("sort")
    @Schema(description = "排序")
    private Integer sort;

    @TableField("status")
    @Schema(description = "状态 1-启用,0-禁用")
    private Integer status;

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
