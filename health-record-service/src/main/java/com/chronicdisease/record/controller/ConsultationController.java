package com.chronicdisease.record.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.ConsultationPageDTO;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import com.chronicdisease.record.domain.vo.DoctorPatientVO;
import com.chronicdisease.record.service.IConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultation")
@Slf4j
@Tag(name = "医患沟通", description = "医患在线聊天相关接口")
public class ConsultationController {

    @Autowired
    private IConsultationService consultationService;

    @OperationLog(module = "医患沟通", description = "分页查询聊天历史")
    @PostMapping("/page")
    @Operation(summary = "分页查询聊天历史（按时间正序）")
    public Result<PageResult<ConsultationRecord>> page(@Valid @RequestBody ConsultationPageDTO dto) {
        return Result.success(consultationService.pageHistory(dto));
    }

    @OperationLog(module = "医患沟通", description = "标记已读")
    @PutMapping("/read")
    @Operation(summary = "标记当前用户与指定对象的未读消息为已读")
    public Result<Void> read(@RequestParam("patientId") Long patientId,
                             @RequestParam("doctorId") Long doctorId) {
        Long readerId = UserInfoContext.getUserId();
        consultationService.markRead(readerId, patientId, doctorId);
        return Result.success();
    }

    @OperationLog(module = "医患沟通", description = "医生查看患者列表")
    @GetMapping("/doctor/patients")
    @Operation(summary = "医生端：获取我的患者列表（含最新消息、未读数、在线状态）")
    public Result<List<DoctorPatientVO>> doctorPatients() {
        Long doctorId = UserInfoContext.getUserId();
        return Result.success(consultationService.getDoctorPatients(doctorId));
    }
}
