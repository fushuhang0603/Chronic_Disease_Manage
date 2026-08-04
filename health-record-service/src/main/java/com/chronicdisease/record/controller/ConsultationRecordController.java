package com.chronicdisease.record.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.dto.AdminConsultationPageDTO;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.dto.DayConsultationDTO;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.ChatRecordVO;
import com.chronicdisease.record.service.IConsultationRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultation")
@Slf4j
@Tag(name = "在线问诊", description = "医患聊天记录相关接口")
public class ConsultationRecordController {

    @Autowired
    private IConsultationRecordService consultationRecordService;

    @OperationLog(module = "在线问诊", description = "分页查询聊天记录")
    @PostMapping("/records")
    @Operation(summary = "分页查询当前用户与对端用户的聊天记录（时间倒序）")
    public Result<PageResult<ChatRecordVO>> records(@RequestBody ConsultationPageDTO dto) {
        return Result.success(consultationRecordService.pageRecords(dto));
    }

    @OperationLog(module = "在线问诊", description = "标记会话消息已读")
    @PostMapping("/read")
    @Operation(summary = "将当前用户与对端用户会话中的未读消息标记为已读")
    public Result<Void> read(@RequestParam("otherUserId") Long otherUserId) {
        consultationRecordService.markRead(otherUserId);
        return Result.success();
    }

    @OperationLog(module = "管理端聊天", description = "分页查询管理端咨询记录")
    @PostMapping("/admin/records")
    @Operation(summary = "分页查询管理端咨询记录")
    public Result<PageResult<ConsultationRecord>> adminRecords(@RequestBody AdminConsultationPageDTO dto){
         PageResult<ConsultationRecord> result = consultationRecordService.pageAdminPage(dto);
         return Result.success(result);
    }

    @OperationLog(module = "管理端聊天", description = "查询管理端今日聊天记录")
    @PostMapping("/admin/Day/records")
    @Operation(summary = "查询管理端今日聊天记录")
    public Result<List<ConsultationRecord>> DayRecords(@RequestBody DayConsultationDTO dto){
        List<ConsultationRecord> result = consultationRecordService.getDayRecords(dto);
        return Result.success(result);
    }


}
