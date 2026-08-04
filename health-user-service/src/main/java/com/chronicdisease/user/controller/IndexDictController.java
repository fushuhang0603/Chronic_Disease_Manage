package com.chronicdisease.user.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.common.vo.IndexDictBriefVO;
import com.chronicdisease.user.domain.dto.IndexDictDTO;
import com.chronicdisease.user.domain.entity.IndexDict;
import com.chronicdisease.user.domain.query.IndexDictQuery;
import com.chronicdisease.user.service.IIndexDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dict")
@Slf4j
@Tag(name = "指标字典管理", description = "指标字典相关接口")
public class IndexDictController {

    @Autowired
    private IIndexDictService indexDictService;

    @OperationLog(module = "指标字典", description = "分页查询指标字典")
    @PostMapping("/page")
    @Operation(summary = "分页查询指标字典")
    public Result<PageResult<IndexDict>> page(@RequestBody IndexDictQuery query) {
        PageResult<IndexDict> result = indexDictService.getPage(query);
        return Result.success(result);
    }

    @OperationLog(module = "指标字典", description = "新增指标字典")
    @PostMapping("/add")
    @Operation(summary = "新增指标字典")
    public Result<Void> add(@Valid @RequestBody IndexDictDTO dto) {
        indexDictService.addDict(dto);
        return Result.success();
    }

    @OperationLog(module = "指标字典", description = "编辑指标字典")
    @PostMapping("/edit")
    @Operation(summary = "编辑指标字典")
    public Result<Void> edit(@Valid @RequestBody IndexDictDTO dto) {
        indexDictService.editDict(dto);
        return Result.success();
    }

    @OperationLog(module = "指标字典", description = "根据ID查询指标字典")
    @GetMapping("/queryById")
    @Operation(summary = "根据ID查询指标字典")
    public Result<IndexDict> queryById(@RequestParam("id") Long id) {
        IndexDict result = indexDictService.queryById(id);
        return Result.success(result);
    }

    @OperationLog(module = "指标字典", description = "根据编码查询指标字典")
    @GetMapping("/getByCode")
    @Operation(summary = "根据编码查询指标字典（含正常范围阈值）")
    public Result<IndexDictBriefVO> getDictByCode(@RequestParam("indexCode") String indexCode) {
        return Result.success(indexDictService.queryByCode(indexCode));
    }

    @OperationLog(module = "指标字典", description = "删除指标字典")
    @PostMapping("/delete")
    @Operation(summary = "删除指标字典")
    public Result<Void> delete(@RequestParam("id") Long id) {
        indexDictService.deleteById(id);
        return Result.success();
    }

    @OperationLog(module = "指标字典", description = "更新指标字典状态")
    @PostMapping("/updateStatus")
    @Operation(summary = "启用/禁用指标字典")
    public Result<Void> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        indexDictService.updateStatus(id, status);
        return Result.success();
    }
}
