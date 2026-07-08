package com.chronicdisease.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.user.domain.dto.IndexDictDTO;
import com.chronicdisease.user.domain.entity.IndexDict;
import com.chronicdisease.user.domain.query.IndexDictQuery;
import com.chronicdisease.user.service.Impl.Impl.IndexDictServiceImpl;
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
    private IndexDictServiceImpl indexDictService;

    @GetMapping("/page")
    @Operation(summary = "分页查询指标字典")
    public Result<IPage<IndexDict>> page(IndexDictQuery query) {
        log.info("分页查询指标字典,{}", query);
        IPage<IndexDict> result = indexDictService.getPage(query);
        log.info("分页查询指标字典结果,{}", result);
        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "新增指标字典")
    public Result<Void> add(@Valid @RequestBody IndexDictDTO dto) {
        log.info("新增指标字典,{}", dto);
        indexDictService.addDict(dto);
        return Result.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑指标字典")
    public Result<Void> edit(@Valid @RequestBody IndexDictDTO dto) {
        log.info("编辑指标字典,{}", dto);
        indexDictService.editDict(dto);
        return Result.success();
    }

    @GetMapping("/queryById")
    @Operation(summary = "根据ID查询指标字典")
    public Result<IndexDict> queryById(@RequestParam("id") Long id) {
        log.info("根据ID查询指标字典, id={}", id);
        IndexDict result = indexDictService.queryById(id);
        return Result.success(result);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除指标字典")
    public Result<Void> delete(@RequestParam("id") Long id) {
        log.info("删除指标字典, id={}", id);
        indexDictService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "启用/禁用指标字典")
    public Result<Void> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        log.info("更新指标字典状态, id={}, status={}", id, status);
        indexDictService.updateStatus(id, status);
        return Result.success();
    }
}
