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
import com.chronicdisease.user.mapper.UserMapper;
import com.chronicdisease.user.service.IDoctorProfileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorProfileServiceImpl extends ServiceImpl<DoctorProfileMapper, DoctorProfile> implements IDoctorProfileService {
    @Autowired
    private DoctorProfileMapper doctorProfileMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> getDoctorUserList() {
        return doctorProfileMapper.getDoctorUserList();
    }

    @Override
    public PageResult<DoctorProfile> getPage(DoctorProfileQuery query) {
        Page<DoctorProfile> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<DoctorProfile> result = doctorProfileMapper.selectPageWithUser(
                page, query.getDoctorName(), query.getHospital(), query.getDepartment());
        return new PageResult<>(result.getRecords(), result.getTotal());
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
        LambdaQueryWrapper<DoctorProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorProfile::getDoctorId, id);
        DoctorProfile profile = baseMapper.selectOne(wrapper);
        if (profile == null) {
            throw new BusinessException("医生资历不存在");
        }
        User user = userMapper.selectById(profile.getDoctorId());
        if (user != null) {
            profile.setDoctorName(user.getUsername());
        }
        return profile;
    }

    @Override
    public void deleteById(Long id) {
        DoctorProfile profile = baseMapper.selectById(id);
        if (profile == null) {
            throw new BusinessException("医生资历不存在");
        }
        baseMapper.deleteById(id);
    }
}
