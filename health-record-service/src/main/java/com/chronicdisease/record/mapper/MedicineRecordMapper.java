package com.chronicdisease.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.record.domain.entity.MedicineRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MedicineRecordMapper extends BaseMapper<MedicineRecord> {
}
