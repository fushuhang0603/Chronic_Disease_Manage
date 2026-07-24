package com.chronicdisease.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.DoctorProfileDTO;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.DoctorProfileQuery;
import jakarta.validation.Valid;

import java.util.List;

public interface IDoctorProfileService extends IService<DoctorProfile> {
    List<User> getDoctorUserList();

    PageResult<DoctorProfile> getPage(DoctorProfileQuery query);

    void addProfile(@Valid DoctorProfileDTO dto);

    void editProfile(@Valid DoctorProfileDTO dto);

    DoctorProfile queryById(Long id);

    void deleteById(Long id);

    List<DoctorProfile> getDoctorList();
}
