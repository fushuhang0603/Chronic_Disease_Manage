package com.chronicdisease.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.user.domain.entity.DoctorPatient;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorPatientMapper extends BaseMapper<DoctorPatient> {
}
