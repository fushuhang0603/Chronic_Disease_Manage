package com.chronicdisease.remind.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.remind.domain.dto.NoticeDTO;
import com.chronicdisease.remind.domain.entity.Notice;

public interface INoticeService extends IService<Notice> {
    void addNotice(NoticeDTO noticeDTO);
}
