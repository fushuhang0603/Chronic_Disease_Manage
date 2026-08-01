package com.chronicdisease.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.user.domain.entity.DoctorPatient;
import com.chronicdisease.user.domain.vo.UserInfoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorPatientMapper extends BaseMapper<DoctorPatient> {
    List<UserInfoVO> getMyPatients(Long doctorId);
}
