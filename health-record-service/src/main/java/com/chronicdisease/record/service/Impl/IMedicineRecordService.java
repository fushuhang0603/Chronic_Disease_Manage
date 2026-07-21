package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.record.domain.dto.MedicineRecordDTO;
import com.chronicdisease.record.domain.dto.MedicineRecordPageDTO;
import com.chronicdisease.record.domain.entity.MedicineRecord;

public interface IMedicineRecordService extends IService<MedicineRecord> {

    void addRecord(MedicineRecordDTO dto);

    PageResult<MedicineRecord> pageRecords(MedicineRecordPageDTO dto);

    void deleteRecord(Long id);

    PageResult<MedicineRecord> pageAdminRecords(String patientName, Integer pageNum, Integer pageSize);
}
