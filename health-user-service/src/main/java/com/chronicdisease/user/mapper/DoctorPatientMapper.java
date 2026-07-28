package com.chronicdisease.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.user.domain.entity.DoctorPatient;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorPatientMapper extends BaseMapper<DoctorPatient> {
    List<Long> getMyPatients(Long doctorId);
}
