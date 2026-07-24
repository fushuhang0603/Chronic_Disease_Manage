package com.chronicdisease.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorProfileMapper extends BaseMapper<DoctorProfile> {

    List<User> getDoctorUserList();

    Page<DoctorProfile> selectPageWithUser(Page<DoctorProfile> page,
                                           @Param("doctorName") String doctorName,
                                           @Param("hospital") String hospital,
                                           @Param("department") String department);
}
