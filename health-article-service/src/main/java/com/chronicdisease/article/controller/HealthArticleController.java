package com.chronicdisease.article.controller;

import com.chronicdisease.article.domain.dto.*;
import com.chronicdisease.article.domain.entity.HealthArticle;
import com.chronicdisease.article.service.IHealthArticleService;
import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "健康资讯管理")
@RestController
@RequestMapping("/article")
public class HealthArticleController {

    @Resource
    private IHealthArticleService healthArticleService;

    @OperationLog(module = "资讯管理", description = "分页查询资讯")
    @PostMapping("/page")
    @Operation(summary = "分页查询资讯")
    public Result<PageResult<HealthArticle>> page(@RequestBody ArticlePageDTO dto) {
        return Result.success(healthArticleService.pageArticle(dto));
    }

    @OperationLog(module = "资讯管理", description = "查看资讯详情")
    @GetMapping("/detail")
    @Operation(summary = "查看资讯详情")
    public Result<HealthArticle> detail(@RequestParam("id") Long id) {
        return Result.success(healthArticleService.getDetail(id));
    }

    @OperationLog(module = "资讯管理", description = "新增资讯")
    @PostMapping("/add")
    @Operation(summary = "新增资讯")
    public Result<Void> add(@Valid @RequestBody ArticleSaveDTO dto) {
        healthArticleService.addArticle(dto);
        return Result.success();
    }

    @OperationLog(module = "资讯管理", description = "编辑资讯")
    @PostMapping("/edit")
    @Operation(summary = "编辑资讯")
    public Result<Void> edit(@Valid @RequestBody ArticleSaveDTO dto) {
        healthArticleService.updateArticle(dto);
        return Result.success();
    }

    @OperationLog(module = "资讯管理", description = "删除资讯")
    @PostMapping("/delete")
    @Operation(summary = "删除资讯")
    public Result<Void> delete(@RequestParam("id") Long id) {
        healthArticleService.deleteArticle(id);
        return Result.success();
    }

    @OperationLog(module = "资讯管理", description = "上下架资讯")
    @PostMapping("/updateStatus")
    @Operation(summary = "上下架资讯")
    public Result<Void> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        healthArticleService.updateStatus(id, status);
        return Result.success();
    }

    @OperationLog(module = "资讯管理", description = "收藏/取消收藏")
    @PostMapping("/favorite")
    @Operation(summary = "收藏/取消收藏")
    public Result<Void> favorite(@RequestBody FavoriteDTO dto) {
        healthArticleService.toggleFavorite(dto);
        return Result.success();
    }

    @OperationLog(module = "资讯管理", description = "记录阅读历史")
    @PostMapping("/readHistory")
    @Operation(summary = "记录阅读历史")
    public Result<Void> readHistory(@RequestBody ReadHistoryDTO dto) {
        healthArticleService.recordReadHistory(dto);
        return Result.success();
    }
}
