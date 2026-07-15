package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.HealthArchiveDTO;
import com.chronicdisease.user.domain.entity.HealthArchive;
import com.chronicdisease.user.domain.query.HealthArchiveQuery;
import com.chronicdisease.user.domain.vo.PatientBriefVO;

import java.util.List;

public interface IHealthArchiveService extends IService<HealthArchive> {
    HealthArchive getMyArchive();
    void addArchive(HealthArchiveDTO dto);
    void editArchive(HealthArchiveDTO dto);
    PageResult<HealthArchive> pageArchive(HealthArchiveQuery query);
    void deleteArchive(Long id);

    List<Long> searchUserIds(String patientName);

    /** 获取全部患者 userId → 姓名映射 */
    List<PatientBriefVO> getAllPatientBriefs();
}
