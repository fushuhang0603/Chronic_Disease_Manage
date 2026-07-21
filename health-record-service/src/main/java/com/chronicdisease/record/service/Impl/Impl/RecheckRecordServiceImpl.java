package com.chronicdisease.record.service.Impl.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.RecheckRecordDTO;
import com.chronicdisease.record.domain.dto.RecheckRecordPageDTO;
import com.chronicdisease.record.domain.entity.MedicineRecord;
import com.chronicdisease.record.domain.entity.RecheckRecord;
import com.chronicdisease.record.domain.vo.PatientBriefVO;
import com.chronicdisease.record.feign.UserServiceFeign;
import com.chronicdisease.record.mapper.RecheckRecordMapper;
import com.chronicdisease.record.service.Impl.IRecheckRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RecheckRecordServiceImpl extends ServiceImpl<RecheckRecordMapper, RecheckRecord> implements IRecheckRecordService {

    @Autowired
    private RecheckRecordMapper recheckRecordMapper;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Override
    public void addRecord(RecheckRecordDTO dto) {
        Long userId = UserInfoContext.getUserId();
        RecheckRecord record = new RecheckRecord();
        record.setUserId(userId);
        record.setHospitalName(dto.getHospitalName());
        record.setRecheckItemCode(dto.getRecheckItemCode());
        record.setRecheckResult(dto.getRecheckResult());
        record.setDoctorAdvice(dto.getDoctorAdvice());
        record.setActualRecheckTime(dto.getActualRecheckTime());
        record.setPlanNextTime(dto.getPlanNextTime());
        recheckRecordMapper.insert(record);
        log.info("复查记录录入成功, userId={}, recheckItemCode={}", userId, dto.getRecheckItemCode());
    }

    @Override
    public PageResult<RecheckRecord> pageRecords(RecheckRecordPageDTO dto) {
        Long userId = UserInfoContext.getUserId();
        LambdaQueryWrapper<RecheckRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecheckRecord::getUserId, userId);
        wrapper.eq(RecheckRecord::getIsDeleted, BusinessConstant.isNotDelete);
        if (dto.getRecheckItemCode() != null && !dto.getRecheckItemCode().isEmpty()) {
            wrapper.eq(RecheckRecord::getRecheckItemCode, dto.getRecheckItemCode());
        }
        wrapper.orderByDesc(RecheckRecord::getActualRecheckTime);
        Page<RecheckRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<RecheckRecord> dbResult = recheckRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(dbResult.getRecords(), dbResult.getTotal());
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = UserInfoContext.getUserId();
        LambdaUpdateWrapper<RecheckRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RecheckRecord::getId, id)
                .eq(RecheckRecord::getUserId, userId)
                .set(RecheckRecord::getIsDeleted, BusinessConstant.isDelete);
        recheckRecordMapper.update(wrapper);
        log.info("复查记录删除成功, id={}, userId={}", id, userId);
    }

    @Override
    public PageResult<RecheckRecord> pageAdminRecords(String patientName, Integer pageNum, Integer pageSize) {
        Map<Long, String> map = new HashMap<>();
        //根据患者姓名查询简要信息
        try {
            List<PatientBriefVO> patientBriefs = userServiceFeign.getAllPatientBriefs(patientName).getData();
            map = patientBriefs.stream().collect(Collectors.toMap(PatientBriefVO::getUserId, PatientBriefVO::getPatientName,(a,b)-> a));
        } catch (Exception e) {
            //降级处理
            log.warn("获取患者信息失败, patientName={}", patientName, e);
        }
        LambdaQueryWrapper<RecheckRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecheckRecord::getIsDeleted, BusinessConstant.isNotDelete);
        if(StringUtils.isNotBlank(patientName)){
            if(CollUtil.isEmpty(map)){
                return new PageResult<>();
            }
            wrapper.in(RecheckRecord::getUserId, map.keySet());
        }
        wrapper.orderByDesc(RecheckRecord::getCreateTime);
        Page<RecheckRecord> page = new Page<>(pageNum, pageSize);
        Page<RecheckRecord> result = recheckRecordMapper.selectPage(page, wrapper);
        //患者姓名回填
        List<RecheckRecord> records = result.getRecords();
        if (CollUtil.isNotEmpty(records)){
            Map<Long, String> nameMap = map;
            records.forEach(record->record.setPatientName(nameMap.getOrDefault(record.getUserId(),"-")));
        }
        return new PageResult<>(records, result.getTotal());
    }
}
