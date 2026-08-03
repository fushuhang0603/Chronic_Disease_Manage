package com.chronicdisease.remind.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.remind.domain.dto.NoticeDTO;
import com.chronicdisease.remind.domain.entity.Notice;
import com.chronicdisease.remind.mapper.NoticeMapper;
import com.chronicdisease.remind.service.INoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public void addNotice(NoticeDTO noticeDTO) {
        Notice notice = new Notice();
        BeanUtil.copyProperties(noticeDTO, notice);
        notice.setPublisherId(UserInfoContext.getUserId());
        //设置查看范围
        if(StringUtils.isBlank(notice.getScope())){
            notice.setScope("all");
        }
        notice.setStatus(BusinessConstant.Notice_Status_Draft);
        notice.setPublishTime(null);
        notice.setIsDeleted(BusinessConstant.isNotDelete);
        noticeMapper.insert(notice);
    }
}
