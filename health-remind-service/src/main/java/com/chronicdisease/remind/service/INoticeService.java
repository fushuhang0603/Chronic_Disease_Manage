package com.chronicdisease.remind.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.remind.domain.dto.NoticeDTO;
import com.chronicdisease.remind.domain.dto.NoticePageDTO;
import com.chronicdisease.remind.domain.entity.Notice;

public interface INoticeService extends IService<Notice> {
    void addNotice(NoticeDTO noticeDTO);


    PageResult<Notice> pageNotice(NoticePageDTO noticePageDTO);


    Notice getNoticeById(Long id);


    void updateNotice(NoticeDTO noticeDTO);


    void updateNoticeStatus(Long id, Integer status);

    void deleteNotice(Long id);
}
