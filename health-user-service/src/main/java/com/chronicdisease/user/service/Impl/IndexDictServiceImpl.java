package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.IndexDictDTO;
import com.chronicdisease.user.domain.entity.IndexDict;
import com.chronicdisease.user.domain.query.IndexDictQuery;
import com.chronicdisease.user.domain.vo.IndexDictBriefVO;
import com.chronicdisease.user.mapper.IndexDictMapper;
import com.chronicdisease.user.service.IIndexDictService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class IndexDictServiceImpl extends ServiceImpl<IndexDictMapper, IndexDict> implements IIndexDictService {

    @Override
    public PageResult<IndexDict> getPage(IndexDictQuery query) {
        Page<IndexDict> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<IndexDict> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(query.getIndexCode())) {
            wrapper.like(IndexDict::getIndexCode, query.getIndexCode());
        }
        if (StringUtils.isNotBlank(query.getIndexName())) {
            wrapper.like(IndexDict::getIndexName, query.getIndexName());
        }
        if (StringUtils.isNotBlank(query.getTermType())) {
            wrapper.eq(IndexDict::getTermType, query.getTermType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(IndexDict::getStatus, query.getStatus());
        }
        wrapper.eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete);
        wrapper.orderByAsc(IndexDict::getTermType)
                .orderByAsc(IndexDict::getSort);
        Page<IndexDict> result = baseMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public void addDict(IndexDictDTO dto) {
        // 唯一性校验（仅校验未删除数据）
        LambdaQueryWrapper<IndexDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IndexDict::getIndexCode, dto.getIndexCode())
                .eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete);
        if (baseMapper.selectOne(queryWrapper) != null) {
            throw new BusinessException("术语编码已存在");
        }
        IndexDict entity = new IndexDict();
        copyDtoToEntity(dto, entity);
        baseMapper.insert(entity);
    }

    @Override
    public void editDict(IndexDictDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("ID不能为空");
        }
        // 编码唯一性校验（排除自身）
        LambdaQueryWrapper<IndexDict> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(IndexDict::getIndexCode, dto.getIndexCode())
                .eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete)
                .ne(IndexDict::getId, dto.getId());
        if (baseMapper.selectOne(existWrapper) != null) {
            throw new BusinessException("术语编码已存在");
        }
        LambdaQueryWrapper<IndexDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IndexDict::getId, dto.getId()).eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete);
        IndexDict entity = baseMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new BusinessException("术语不存在");
        }
        copyDtoToEntity(dto, entity);
        baseMapper.updateById(entity);
    }

    @Override
    public IndexDict queryById(Long id) {
        LambdaQueryWrapper<IndexDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IndexDict::getId, id).eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete);
        IndexDict entity = baseMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("术语不存在");
        }
        return entity;
    }

    @Override
    public IndexDictBriefVO queryByCode(String indexCode) {
        LambdaQueryWrapper<IndexDict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IndexDict::getIndexCode, indexCode)
                .eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete);
        IndexDict dict = baseMapper.selectOne(wrapper);
        if (dict == null) {
            return null;
        }
        IndexDictBriefVO vo = new IndexDictBriefVO();
        vo.setIndexCode(dict.getIndexCode());
        vo.setIndexName(dict.getIndexName());
        vo.setTermType(dict.getTermType());
        vo.setMinValue(dict.getMinValue());
        vo.setMaxValue(dict.getMaxValue());
        vo.setStatus(dict.getStatus());
        return vo;
    }

    @Override
    public void deleteById(Long id) {
        LambdaQueryWrapper<IndexDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IndexDict::getId, id).eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete);
        IndexDict entity = baseMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new BusinessException("术语不存在");
        }
        LambdaUpdateWrapper<IndexDict> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(IndexDict::getId, id).set(IndexDict::getIsDeleted, BusinessConstant.isDelete);
        baseMapper.update(wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        LambdaQueryWrapper<IndexDict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(IndexDict::getId, id).eq(IndexDict::getIsDeleted, BusinessConstant.isNotDelete);
        IndexDict entity = baseMapper.selectOne(queryWrapper);
        if (entity == null) {
            throw new BusinessException("术语不存在");
        }
        LambdaUpdateWrapper<IndexDict> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(IndexDict::getId, id).set(IndexDict::getStatus, status);
        baseMapper.update(wrapper);
    }

    private void copyDtoToEntity(IndexDictDTO dto, IndexDict entity) {
        entity.setIndexCode(dto.getIndexCode());
        entity.setIndexName(dto.getIndexName());
        entity.setTermType(dto.getTermType());
        entity.setMinValue(dto.getMinValue());
        entity.setMaxValue(dto.getMaxValue());
        entity.setSort(dto.getSort());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
    }
}
