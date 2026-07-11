package com.chronicdisease.remind.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.remind.domain.dto.HealthRemindDTO;
import com.chronicdisease.remind.domain.dto.HealthRemindPageDTO;
import com.chronicdisease.remind.domain.entity.HealthRemind;
import com.chronicdisease.remind.mapper.HealthRemindMapper;
import com.chronicdisease.remind.service.IHealthRemindService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HealthRemindServiceImpl extends ServiceImpl<HealthRemindMapper, HealthRemind> implements IHealthRemindService {

    @Autowired
    private HealthRemindMapper healthRemindMapper;

    @Override
    public void addRemind(HealthRemindDTO dto) {
        Long userId = UserInfoContext.getUserId();

        if (StringUtils.isBlank(dto.getRemindType())) {
            throw new BusinessException("提醒类型不能为空");
        }
        if (StringUtils.isBlank(dto.getTitle())) {
            throw new BusinessException("提醒标题不能为空");
        }
        if (dto.getRemindTime() == null) {
            throw new BusinessException("提醒时间不能为空");
        }
        HealthRemind healthRemind = new HealthRemind();
        BeanUtils.copyProperties(dto, healthRemind);
        healthRemind.setUserId(userId);
        if (StringUtils.isBlank(healthRemind.getRepeatType())) {
            healthRemind.setRepeatType(BusinessConstant.Repeat_Type1);
        }
        healthRemind.setRemindStatus(BusinessConstant.Remind_Status1);

        healthRemindMapper.insert(healthRemind);
    }

    @Override
    public PageResult<HealthRemind> pageRemind(HealthRemindPageDTO dto) {
        return null;
    }

    @Override
    public void updateStatus(Long id, Integer status) {

    }

    @Override
    public void deleteRemind(Long id) {

    }

    @Override
    public List<HealthRemind> upcomingRemind() {
        return List.of();
    }
}
