package com.chronicdisease.remind.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.remind.domain.dto.NoticeDTO;
import com.chronicdisease.remind.domain.dto.NoticePageDTO;
import com.chronicdisease.remind.domain.entity.Notice;
import com.chronicdisease.remind.mapper.NoticeMapper;
import com.chronicdisease.remind.service.INoticeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    @Override
    public PageResult<Notice> pageNotice(NoticePageDTO noticePageDTO) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getIsDeleted, BusinessConstant.isNotDelete);
        if (StringUtils.isNotBlank(noticePageDTO.getTitle())) {
            wrapper.like(Notice::getTitle, noticePageDTO.getTitle());
        }
        if (noticePageDTO.getStatus() != null) {
            wrapper.eq(Notice::getStatus, noticePageDTO.getStatus());
        }
        if (StringUtils.isNotBlank(noticePageDTO.getScope())) {
            wrapper.eq(Notice::getScope, noticePageDTO.getScope());
        }
        wrapper.eq(Notice::getIsDeleted,BusinessConstant.isNotDelete);
        wrapper.orderByDesc(Notice::getPublishTime).orderByDesc(Notice::getCreateTime);
        Page<Notice> page = new Page<>(noticePageDTO.getPageNum(), noticePageDTO.getPageSize());
        Page<Notice> results = noticeMapper.selectPage(page, wrapper);
        return new PageResult<>(results.getRecords(), results.getTotal());
    }

    @Override
    public Notice getNoticeById(Long id) {
        return noticeMapper.selectOne(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getId, id)
                .eq(Notice::getIsDeleted, BusinessConstant.isNotDelete));
    }

    @Override
    public void updateNotice(NoticeDTO noticeDTO) {
        Notice notice = noticeMapper.selectById(noticeDTO.getId());
        if (notice == null || BusinessConstant.isDelete.equals(notice.getIsDeleted())) {
            throw new BusinessException("公告不存在");
        }
        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        if (StringUtils.isNotBlank(noticeDTO.getScope())) {
            notice.setScope(noticeDTO.getScope());
        }
        //编辑不修改状态、发布人、发布时间
        noticeMapper.updateById(notice);
    }

    @Override
    public void updateNoticeStatus(Long id, Integer status) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || BusinessConstant.isDelete.equals(notice.getIsDeleted())) {
            throw new BusinessException("公告不存在");
        }
        Notice update = new Notice();
        update.setId(id);
        update.setStatus(status);
        //发布或重新发布时刷新发布时间
        if (BusinessConstant.Notice_Status_Published.equals(status)) {
            update.setPublishTime(LocalDateTime.now());
        }
        noticeMapper.updateById(update);
    }
}
