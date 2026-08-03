package com.chronicdisease.remind.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.remind.domain.dto.NoticeDTO;
import com.chronicdisease.remind.domain.dto.NoticePageDTO;
import com.chronicdisease.remind.domain.entity.Notice;
import com.chronicdisease.remind.service.INoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notice")
@Slf4j
@Tag(name ="公告管理",description = "系统公告相关接口")
public class NoticeController {
    @Autowired
    private INoticeService noticeService;


    @PostMapping("/add")
    @OperationLog(module = "公告管理",description = "添加公告")
    @Operation(summary = "添加公告", description = "添加公告")
    public Result add(@RequestBody NoticeDTO noticeDTO){
           noticeService.addNotice(noticeDTO);
           return Result.success();
    }

    @PostMapping("/page")
    @OperationLog(module = "公告管理", description = "分页查询公告")
    @Operation(summary = "分页查询公告", description = "分页查询公告")
    public Result<PageResult<Notice>> page(@RequestBody NoticePageDTO noticePageDTO){
        PageResult<Notice> result = noticeService.pageNotice(noticePageDTO);
        return Result.success(result);
    }

    @GetMapping("/queryById")
    @OperationLog(module = "公告管理", description = "查询公告详情")
    @Operation(summary = "查询公告详情", description = "根据公告ID查询详情")
    public Result<Notice> queryById(@RequestParam("id") Long id){
        return Result.success(noticeService.getNoticeById(id));
    }

    @PostMapping("/update")
    @OperationLog(module = "公告管理", description = "编辑公告")
    @Operation(summary = "编辑公告", description = "编辑公告标题、内容、范围")
    public Result update(@RequestBody NoticeDTO noticeDTO){
        noticeService.updateNotice(noticeDTO);
        return Result.success();
    }

    @PostMapping("/updateStatus")
    @OperationLog(module = "公告管理", description = "更新公告状态")
    @Operation(summary = "更新公告状态", description = "发布/下线公告")
    public Result updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status){
        noticeService.updateNoticeStatus(id, status);
        return Result.success();
    }

    



}
