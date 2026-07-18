package com.chronicdisease.record.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;

import java.util.List;
import java.util.Map;

public interface IHealthIndexService extends IService<HealthIndexRecord> {

    void addRecord(HealthIndexDTO dto);

    PageResult<HealthIndexRecord> pageRecords(HealthIndexPageDTO dto);

    Map<String, List<HealthIndexRecord>> getChart(Integer days);

    void deleteRecord(Long id);

    PageResult<HealthIndexRecord> pageRecordsByPatientName(String patientName, Integer pageNum, Integer pageSize, String indexCode);
}
