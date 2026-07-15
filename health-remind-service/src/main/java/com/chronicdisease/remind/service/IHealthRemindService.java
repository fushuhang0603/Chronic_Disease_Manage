package com.chronicdisease.remind.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.remind.domain.dto.HealthRemindAdminPageDTO;
import com.chronicdisease.remind.domain.dto.HealthRemindDTO;
import com.chronicdisease.remind.domain.dto.HealthRemindPageDTO;
import com.chronicdisease.remind.domain.entity.HealthRemind;

public interface IHealthRemindService extends IService<HealthRemind> {

    void addRemind(HealthRemindDTO dto);

    PageResult<HealthRemind> pageRemind(HealthRemindPageDTO dto);

    void updateStatus(Long id, Integer status);

    void deleteRemind(Long id);

    /** 管理端分页查询全部患者提醒 */
    PageResult<HealthRemind> pageAllRemind(HealthRemindAdminPageDTO dto);
}
