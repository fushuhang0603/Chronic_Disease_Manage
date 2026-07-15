package com.chronicdisease.user.service.Impl.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.user.domain.dto.HealthArchiveDTO;
import com.chronicdisease.user.domain.entity.HealthArchive;
import com.chronicdisease.user.domain.query.HealthArchiveQuery;
import com.chronicdisease.user.mapper.HealthArchiveMapper;
import com.chronicdisease.user.service.Impl.IHealthArchiveService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthArchiveServiceImpl extends ServiceImpl<HealthArchiveMapper, HealthArchive> implements IHealthArchiveService {

    @Override
    public HealthArchive getMyArchive() {
        Long userId = UserInfoContext.getUserId();
        LambdaQueryWrapper<HealthArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthArchive::getUserId, userId).eq(HealthArchive::getIsDeleted, 0);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public void addArchive(HealthArchiveDTO dto) {
        Long userId = UserInfoContext.getUserId();
        // 检查是否已有档案
        LambdaQueryWrapper<HealthArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthArchive::getUserId, userId).eq(HealthArchive::getIsDeleted, 0);
        if (baseMapper.selectOne(wrapper) != null) {
            throw new BusinessException("已有健康档案，请刷新页面后编辑");
        }
        HealthArchive archive = new HealthArchive();
        copyDtoToEntity(dto, archive);
        archive.setUserId(userId);
        this.save(archive);
    }

    @Override
    public void editArchive(HealthArchiveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("档案ID不能为空");
        }
        Long userId = UserInfoContext.getUserId();
        HealthArchive archive = baseMapper.selectById(dto.getId());
        if (archive == null || !archive.getUserId().equals(userId)) {
            throw new BusinessException("档案不存在");
        }
        copyDtoToEntity(dto, archive);
        this.updateById(archive);
    }

    @Override
    public PageResult<HealthArchive> pageArchive(HealthArchiveQuery query) {
        LambdaQueryWrapper<HealthArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthArchive::getIsDeleted, BusinessConstant.isNotDelete);
        if (StringUtils.isNotBlank(query.getPatientName())) {
            wrapper.like(HealthArchive::getPatientName, query.getPatientName());
        }
        if (StringUtils.isNotBlank(query.getIdCard())) {
            wrapper.like(HealthArchive::getIdCard, query.getIdCard());
        }
        wrapper.orderByDesc(HealthArchive::getCreateTime);
        Page<HealthArchive> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<HealthArchive> dbResult = baseMapper.selectPage(page, wrapper);
        return new PageResult<>(dbResult.getRecords(), dbResult.getTotal());
    }

    @Override
    public void deleteArchive(Long id) {
        LambdaQueryWrapper<HealthArchive> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthArchive::getId, id)
                .eq(HealthArchive::getIsDeleted, BusinessConstant.isNotDelete);
        HealthArchive entity = baseMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new BusinessException("档案不存在");
        }
        LambdaUpdateWrapper<HealthArchive> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(HealthArchive::getId, id)
                .set(HealthArchive::getIsDeleted, BusinessConstant.isDelete);
        baseMapper.update(wrapper);
    }

    private void copyDtoToEntity(HealthArchiveDTO dto, HealthArchive archive) {
        archive.setPatientName(dto.getPatientName());
        archive.setPhone(dto.getPhone());
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
