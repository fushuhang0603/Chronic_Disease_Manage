package com.chronicdisease.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.domain.entity.User;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorProfileMapper extends BaseMapper<DoctorProfile> {

    List<User> getDoctorUserList();
}
