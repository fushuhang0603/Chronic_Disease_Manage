package com.chronicdisease.record.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.dto.RecheckRecordDTO;
import com.chronicdisease.record.domain.dto.RecheckRecordPageDTO;
import com.chronicdisease.record.domain.entity.RecheckRecord;
import com.chronicdisease.record.service.Impl.IRecheckRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recheck")
@Slf4j
@Tag(name = "复查记录管理", description = "复查记录相关接口")
public class RecheckRecordController {

    @Autowired
    private IRecheckRecordService recheckRecordService;

    @OperationLog(module = "复查记录", description = "录入复查记录")
    @PostMapping("/add")
    @Operation(summary = "录入复查记录")
    public Result<Void> add(@Valid @RequestBody RecheckRecordDTO dto) {
        recheckRecordService.addRecord(dto);
        return Result.success();
    }

    @OperationLog(module = "复查记录", description = "分页查询复查记录")
    @PostMapping("/page")
    @Operation(summary = "分页查询复查记录")
    public Result<PageResult<RecheckRecord>> page(@RequestBody RecheckRecordPageDTO dto) {
        return Result.success(recheckRecordService.pageRecords(dto));
    }

    @OperationLog(module = "复查记录", description = "删除复查记录")
    @PostMapping("/delete")
    @Operation(summary = "删除复查记录")
    public Result<Void> delete(@RequestParam("id") Long id) {
        recheckRecordService.deleteRecord(id);
        return Result.success();
    }
}
