package com.chronicdisease.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IndexDictDTO {

    private Long id;

    @NotBlank(message = "术语编码不能为空")
    private String indexCode;

    @NotBlank(message = "术语名称不能为空")
    private String indexName;

    @NotBlank(message = "术语类型不能为空")
    private String termType;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    private Integer status;

}
