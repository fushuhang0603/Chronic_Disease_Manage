package com.chronicdisease.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.IndexDictDTO;
import com.chronicdisease.user.domain.entity.IndexDict;
import com.chronicdisease.user.domain.query.IndexDictQuery;
import com.chronicdisease.user.domain.vo.IndexDictBriefVO;

public interface IIndexDictService extends IService<IndexDict> {

    PageResult<IndexDict> getPage(IndexDictQuery query);

    void addDict(IndexDictDTO dto);

    void editDict(IndexDictDTO dto);

    IndexDict queryById(Long id);

    IndexDictBriefVO queryByCode(String indexCode);

    void deleteById(Long id);
    
    void updateStatus(Long id, Integer status);
}
