package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.user.domain.dto.HealthArchiveDTO;
import com.chronicdisease.user.domain.entity.HealthArchive;

public interface IHealthArchiveService extends IService<HealthArchive> {

    HealthArchive getMyArchive(Long userId);

    void addArchive(Long userId, HealthArchiveDTO dto);

    void editArchive(Long userId, HealthArchiveDTO dto);
}
