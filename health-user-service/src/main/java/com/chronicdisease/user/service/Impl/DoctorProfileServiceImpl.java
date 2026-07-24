package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.DoctorProfileDTO;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.DoctorProfileQuery;
import com.chronicdisease.user.mapper.DoctorProfileMapper;
import com.chronicdisease.user.service.IDoctorProfileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorProfileServiceImpl extends ServiceImpl<DoctorProfileMapper, DoctorProfile> implements IDoctorProfileService {
    @Autowired
    private DoctorProfileMapper doctorProfileMapper;

    @Override
    public List<User> getDoctorUserList() {
        return doctorProfileMapper.getDoctorUserList();
    }

    @Override
    public PageResult<DoctorProfile> getPage(DoctorProfileQuery query) {
        return null;
    }

    @Override
    public void addProfile(DoctorProfileDTO dto) {
        // 唯一性校验：一个医生只能有一条资历
        LambdaQueryWrapper<DoctorProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorProfile::getDoctorId, dto.getDoctorId());
        if (baseMapper.selectOne(wrapper) != null) {
            throw new BusinessException("该医生已创建资历，请勿重复添加");
        }
        DoctorProfile entity = new DoctorProfile();
        entity.setDoctorId(dto.getDoctorId());
        entity.setRealName(dto.getRealName());
        entity.setTitle(dto.getTitle());
        entity.setHospital(dto.getHospital());
        entity.setDepartment(dto.getDepartment());
        entity.setSpecialty(dto.getSpecialty());
        entity.setIntroduction(dto.getIntroduction());
        if (dto.getAvatar() != null) {
            entity.setAvatar(dto.getAvatar());
        }
        baseMapper.insert(entity);
    }

    @Override
    public void editProfile(DoctorProfileDTO dto) {

    }

    @Override
    public DoctorProfile queryById(Long id) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
