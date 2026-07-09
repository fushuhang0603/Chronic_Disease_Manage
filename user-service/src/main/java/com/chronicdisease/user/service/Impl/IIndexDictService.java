package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.IndexDictDTO;
import com.chronicdisease.user.domain.entity.IndexDict;
import com.chronicdisease.user.domain.query.IndexDictQuery;

public interface IIndexDictService extends IService<IndexDict> {
    PageResult<IndexDict> getPage(IndexDictQuery query);
    void addDict(IndexDictDTO dto);
    void editDict(IndexDictDTO dto);
    IndexDict queryById(Long id);
    void deleteById(Long id);
    void updateStatus(Long id, Integer status);

    /**
     * 校验术语编码是否存在且启用（供内部Feign调用）
     */
    boolean validateCode(String termCode);

    /**
     * 根据编码解析术语名称（供内部Feign调用）
     */
    String resolveName(String termCode);
}
