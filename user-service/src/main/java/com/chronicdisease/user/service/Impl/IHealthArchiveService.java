package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.user.domain.dto.HealthArchiveDTO;
import com.chronicdisease.user.domain.entity.HealthArchive;

public interface IHealthArchiveService extends IService<HealthArchive> {

    HealthArchive getMyArchive();

    void addArchive(HealthArchiveDTO dto);

    void editArchive(HealthArchiveDTO dto);
}
