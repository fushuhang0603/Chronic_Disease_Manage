package com.chronicdisease.record.service.Impl.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.RecheckRecordDTO;
import com.chronicdisease.record.domain.dto.RecheckRecordPageDTO;
import com.chronicdisease.record.domain.entity.RecheckRecord;
import com.chronicdisease.record.mapper.RecheckRecordMapper;
import com.chronicdisease.record.service.Impl.IRecheckRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecheckRecordServiceImpl extends ServiceImpl<RecheckRecordMapper, RecheckRecord> implements IRecheckRecordService {

    @Autowired
    private RecheckRecordMapper recheckRecordMapper;

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
}
