package com.chronicdisease.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.record.domain.entity.ConsultationRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConsultationRecordMapper extends BaseMapper<ConsultationRecord> {
    List<ConsultationRecord> getRecords(Long doctorId, List<Long> patientIds);
}
