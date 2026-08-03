package com.chronicdisease.remind.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.remind.domain.dto.NoticeDTO;
import com.chronicdisease.remind.domain.dto.NoticePageDTO;
import com.chronicdisease.remind.domain.entity.Notice;

public interface INoticeService extends IService<Notice> {
    void addNotice(NoticeDTO noticeDTO);

    /** 分页查询公告（管理端） */
    PageResult<Notice> pageNotice(NoticePageDTO noticePageDTO);

    /** 根据ID查询公告详情 */
    Notice getNoticeById(Long id);

    /** 编辑公告（标题/内容/范围） */
    void updateNotice(NoticeDTO noticeDTO);

    /** 更新公告状态（1发布 2下线） */
    void updateNoticeStatus(Long id, Integer status);
}
