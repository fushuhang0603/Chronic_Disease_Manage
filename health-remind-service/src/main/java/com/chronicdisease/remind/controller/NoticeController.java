package com.chronicdisease.remind.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.remind.domain.dto.NoticeDTO;
import com.chronicdisease.remind.service.INoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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



}
