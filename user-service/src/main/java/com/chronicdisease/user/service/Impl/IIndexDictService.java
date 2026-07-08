package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.user.domain.dto.IndexDictDTO;
import com.chronicdisease.user.domain.entity.IndexDict;
import com.chronicdisease.user.domain.query.IndexDictQuery;

public interface IIndexDictService extends IService<IndexDict> {
    IPage<IndexDict> getPage(IndexDictQuery query);
    void addDict(IndexDictDTO dto);
    void editDict(IndexDictDTO dto);
    IndexDict queryById(Long id);
    void deleteById(Long id);
    void updateStatus(Long id, Integer status);
}
