package com.chronicdisease.user.service.Impl.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.user.domain.dto.HealthArchiveDTO;
import com.chronicdisease.user.domain.entity.HealthArchive;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.mapper.HealthArchiveMapper;
import com.chronicdisease.user.mapper.UserMapper;
import com.chronicdisease.user.service.Impl.IHealthArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthArchiveServiceImpl extends ServiceImpl<HealthArchiveMapper, HealthArchive> implements IHealthArchiveService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public HealthArchive getMyArchive(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        LambdaQueryWrapper<HealthArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthArchive::getPhone, user.getPhone()).eq(HealthArchive::getIsDeleted, 0);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public void addArchive(Long userId, HealthArchiveDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 检查是否已有档案
        LambdaQueryWrapper<HealthArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthArchive::getPhone, user.getPhone()).eq(HealthArchive::getIsDeleted, 0);
        if (baseMapper.selectOne(wrapper) != null) {
            throw new BusinessException("已有健康档案，请刷新页面后编辑");
        }
        HealthArchive archive = new HealthArchive();
        copyDtoToEntity(dto, archive);
        archive.setPhone(user.getPhone());
        this.save(archive);
    }

    @Override
    public void editArchive(Long userId, HealthArchiveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("档案ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        HealthArchive archive = baseMapper.selectById(dto.getId());
        if (archive == null || !archive.getPhone().equals(user.getPhone())) {
            throw new BusinessException("档案不存在");
        }
        copyDtoToEntity(dto, archive);
        this.updateById(archive);
    }

    private void copyDtoToEntity(HealthArchiveDTO dto, HealthArchive archive) {
        archive.setPatientName(dto.getPatientName());
        archive.setIdCard(dto.getIdCard());
        archive.setBirthDate(dto.getBirthDate());
        archive.setGender(dto.getGender());
        archive.setAddress(dto.getAddress());
        archive.setBloodType(dto.getBloodType());
        archive.setChronicType(dto.getChronicType());
        archive.setDiagnosisDate(dto.getDiagnosisDate());
        archive.setMedicalHistory(dto.getMedicalHistory());
        archive.setFamilyHistory(dto.getFamilyHistory());
        archive.setAllergyHistory(dto.getAllergyHistory());
        archive.setLifeHabit(dto.getLifeHabit());
        archive.setEmergencyName(dto.getEmergencyName());
        archive.setEmergencyPhone(dto.getEmergencyPhone());
    }
}
