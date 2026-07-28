package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.user.domain.entity.DoctorPatient;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.mapper.DoctorPatientMapper;
import com.chronicdisease.user.mapper.DoctorProfileMapper;
import com.chronicdisease.user.mapper.UserMapper;
import com.chronicdisease.user.service.IDoctorPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorPatientServiceImpl extends ServiceImpl<DoctorPatientMapper, DoctorPatient> implements IDoctorPatientService {

    @Autowired
    private DoctorProfileMapper doctorProfileMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<DoctorProfile> getDoctorList() {
        return doctorProfileMapper.selectList(null);
    }

    @Override
    public DoctorProfile getMyDoctor() {
        Long patientId = UserInfoContext.getUserId();
        LambdaQueryWrapper<DoctorPatient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorPatient::getPatientId, patientId)
               .eq(DoctorPatient::getIsDeleted, 0)
               .last("LIMIT 1");
        DoctorPatient binding = baseMapper.selectOne(wrapper);
        if (binding == null) {
            return null;
        }
        return doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getDoctorId, binding.getDoctorId()));
    }

    @Override
    public void bindDoctor(Long doctorId) {
        Long patientId = UserInfoContext.getUserId();
        // 校验医生资历是否存在
        DoctorProfile profile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>().eq(DoctorProfile::getDoctorId, doctorId));
        if (profile == null) {
            throw new BusinessException("医生资历不存在");
        }
        // 查询已有绑定
        LambdaQueryWrapper<DoctorPatient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorPatient::getPatientId, patientId)
               .eq(DoctorPatient::getIsDeleted, 0);
        DoctorPatient existing = baseMapper.selectOne(wrapper);
        if (existing != null) {
            if (existing.getDoctorId().equals(doctorId)) {
                return; // 已绑定同一医生，无需操作
            }
            // 更换医生：更新绑定的医生信息
            existing.setDoctorId(doctorId);
            existing.setDoctorName(profile.getRealName());
            baseMapper.updateById(existing);
            return;
        }
        // 检查是否曾有已删除的绑定记录，有则恢复
        LambdaQueryWrapper<DoctorPatient> deletedWrapper = new LambdaQueryWrapper<>();
        deletedWrapper.eq(DoctorPatient::getPatientId, patientId)
                      .eq(DoctorPatient::getIsDeleted, 1);
        DoctorPatient deleted = baseMapper.selectOne(deletedWrapper);
        if (deleted != null) {
            deleted.setDoctorId(doctorId);
            deleted.setDoctorName(profile.getRealName());
            deleted.setIsDeleted(0);
            baseMapper.updateById(deleted);
            return;
        }
        // 新建绑定
        User patient = userMapper.selectById(patientId);
        DoctorPatient entity = new DoctorPatient();
        entity.setDoctorId(doctorId);
        entity.setDoctorName(profile.getRealName());
        entity.setPatientId(patientId);
        entity.setPatientName(patient != null ? patient.getNickname() : "");
        baseMapper.insert(entity);
    }

    @Override
    public void unbindDoctor() {
        Long patientId = UserInfoContext.getUserId();
        LambdaUpdateWrapper<DoctorPatient> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(DoctorPatient::getPatientId, patientId)
               .eq(DoctorPatient::getIsDeleted, 0)
               .set(DoctorPatient::getIsDeleted, 1);
        baseMapper.update(null, wrapper);
    }

    @Override
    public List<Long> getMyPatients(Long doctorId) {
        LambdaQueryWrapper<DoctorPatient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorPatient::getDoctorId, doctorId)
               .eq(DoctorPatient::getIsDeleted, 0)
               .select(DoctorPatient::getPatientId);
        return baseMapper.selectList(wrapper).stream()
                .map(DoctorPatient::getPatientId)
                .collect(Collectors.toList());
    }
}
