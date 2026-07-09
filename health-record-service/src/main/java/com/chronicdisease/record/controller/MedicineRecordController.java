package com.chronicdisease.record.controller;

import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.record.domain.dto.MedicineRecordDTO;
import com.chronicdisease.record.domain.dto.MedicineRecordPageDTO;
import com.chronicdisease.record.domain.entity.MedicineRecord;
import com.chronicdisease.record.service.Impl.IMedicineRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medicine")
@Slf4j
@Tag(name = "用药记录管理", description = "用药记录相关接口")
public class MedicineRecordController {

    @Autowired
    private IMedicineRecordService medicineRecordService;

    @PostMapping("/add")
    @Operation(summary = "录入用药记录")
    public Result<Void> add(@Valid @RequestBody MedicineRecordDTO dto) {
        log.info("录入用药记录, drugCode={}", dto.getDrugCode());
        medicineRecordService.addRecord(dto);
        return Result.success();
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询用药记录")
    public Result<PageResult<MedicineRecord>> page(@RequestBody MedicineRecordPageDTO dto) {
        log.info("分页查询用药记录, drugCode={}, pageNum={}, pageSize={}",
                dto.getDrugCode(), dto.getPageNum(), dto.getPageSize());
        return Result.success(medicineRecordService.pageRecords(dto));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除用药记录")
    public Result<Void> delete(@RequestParam("id") Long id) {
        log.info("删除用药记录, id={}", id);
        medicineRecordService.deleteRecord(id);
        return Result.success();
    }
}
