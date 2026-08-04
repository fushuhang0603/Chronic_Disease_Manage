package com.chronicdisease.record.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminDashboardVO {

    private Long abnormalPatientCount;

    private List<DashboardAbnormalItem> latestRecords;
}
