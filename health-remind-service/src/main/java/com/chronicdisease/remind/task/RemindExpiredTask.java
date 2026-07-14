package com.chronicdisease.remind.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.remind.domain.entity.HealthRemind;
import com.chronicdisease.remind.mapper.HealthRemindMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RemindExpiredTask {

    @Autowired
    private HealthRemindMapper healthRemindMapper;


    /**
     * 每天凌晨3点扫描不重复类型的提醒，将已过期的标记为"已过期"
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void markExpiredRemind(){
        LambdaQueryWrapper<HealthRemind> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRemind::getRemindType, BusinessConstant.Repeat_Type1);
        wrapper.eq(HealthRemind::getIsDeleted, BusinessConstant.isNotDelete);
        wrapper.in(HealthRemind::getRemindStatus,BusinessConstant.Remind_Status1, BusinessConstant.Remind_Status2);
        int count = healthRemindMapper.update(wrapper);
        log.info("定时任务完成：{} 条提醒已标记为过期", count);
    }
}
