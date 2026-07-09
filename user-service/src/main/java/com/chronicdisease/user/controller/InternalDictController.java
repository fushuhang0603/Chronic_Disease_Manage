package com.chronicdisease.user.controller;

import com.chronicdisease.user.domain.entity.IndexDict;
import com.chronicdisease.user.service.Impl.IIndexDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内部 Feign 接口 —— 术语字典校验与解析
 * 供 health-record-service 通过 OpenFeign 调用
 */
@RestController
@RequestMapping("/internal/dict")
@Slf4j
public class InternalDictController {

    @Autowired
    private IIndexDictService indexDictService;

    /**
     * 校验术语编码是否合法（存在 + 启用）
     */
    @GetMapping("/validate")
    public Boolean validate(@RequestParam("termCode") String termCode) {
        return indexDictService.validateCode(termCode);
    }

    /**
     * 根据编码查询术语名称
     */
    @GetMapping("/resolve")
    public String resolve(@RequestParam("termCode") String termCode) {
        IndexDict dict = indexDictService.getByCode(termCode);
        return dict != null ? dict.getIndexName() : null;
    }

    /**
     * 批量解析编码→名称映射
     */
    @PostMapping("/batchResolve")
    public Map<String, String> batchResolve(@RequestBody List<String> termCodes) {
        return indexDictService.batchGetNames(termCodes);
    }
}
