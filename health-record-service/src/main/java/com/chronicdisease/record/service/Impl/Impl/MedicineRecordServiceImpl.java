package com.chronicdisease.record.service.Impl.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.record.domain.dto.MedicineRecordDTO;
import com.chronicdisease.record.domain.dto.MedicineRecordPageDTO;
import com.chronicdisease.record.domain.entity.MedicineRecord;
import com.chronicdisease.record.mapper.MedicineRecordMapper;
import com.chronicdisease.record.service.Impl.IMedicineRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MedicineRecordServiceImpl extends ServiceImpl<MedicineRecordMapper, MedicineRecord> implements IMedicineRecordService {

    @Autowired
    private MedicineRecordMapper medicineRecordMapper;

    @Override
    public void addRecord(MedicineRecordDTO dto) {
        Long userId = UserInfoContext.getUserId();
        MedicineRecord record = new MedicineRecord();
        record.setUserId(userId);
        record.setDrugCode(dto.getDrugCode());
        record.setDosage(dto.getDosage());
        record.setFrequency(dto.getFrequency());
        record.setStartDate(dto.getStartDate());
        record.setStopDate(dto.getStopDate());
        record.setRemark(dto.getRemark());
        medicineRecordMapper.insert(record);
        log.info("用药记录录入成功, userId={}, drugCode={}", userId, dto.getDrugCode());
    }

    @Override
    public PageResult<MedicineRecord> pageRecords(MedicineRecordPageDTO dto) {
        Long userId = UserInfoContext.getUserId();
        LambdaQueryWrapper<MedicineRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicineRecord::getUserId, userId);
        wrapper.eq(MedicineRecord::getIsDeleted, BusinessConstant.isNotDelete);
        if (dto.getDrugCode() != null && !dto.getDrugCode().isEmpty()) {
            wrapper.eq(MedicineRecord::getDrugCode, dto.getDrugCode());
        }
        wrapper.orderByDesc(MedicineRecord::getStartDate);
        Page<MedicineRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<MedicineRecord> dbResult = medicineRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(dbResult.getRecords(), dbResult.getTotal());
    }

    @Override
    public void deleteRecord(Long id) {
        Long userId = UserInfoContext.getUserId();
        LambdaUpdateWrapper<MedicineRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MedicineRecord::getId, id)
                .eq(MedicineRecord::getUserId, userId)
                .set(MedicineRecord::getIsDeleted, BusinessConstant.isDelete);
        medicineRecordMapper.update(wrapper);
        log.info("用药记录删除成功, id={}, userId={}", id, userId);
    }
}
