package com.chronicdisease.remind.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.remind.domain.dto.HealthRemindDTO;
import com.chronicdisease.remind.domain.dto.HealthRemindPageDTO;
import com.chronicdisease.remind.domain.entity.HealthRemind;

import java.util.List;

public interface IHealthRemindService extends IService<HealthRemind> {

    void addRemind(HealthRemindDTO dto);

    PageResult<HealthRemind> pageRemind(HealthRemindPageDTO dto);

    void readRemind(Long id);

    void completeRemind(Long id);

    void deleteRemind(Long id);

    List<HealthRemind> upcomingRemind();

}
