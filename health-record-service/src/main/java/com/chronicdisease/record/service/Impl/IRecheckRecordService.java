package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.record.domain.dto.RecheckRecordDTO;
import com.chronicdisease.record.domain.dto.RecheckRecordPageDTO;
import com.chronicdisease.record.domain.entity.RecheckRecord;

public interface IRecheckRecordService extends IService<RecheckRecord> {

    void addRecord(RecheckRecordDTO dto);

    PageResult<RecheckRecord> pageRecords(RecheckRecordPageDTO dto);

    void deleteRecord(Long id);

    PageResult<RecheckRecord> pageAdminRecords(String patientName, Integer pageNum, Integer pageSize);
}
