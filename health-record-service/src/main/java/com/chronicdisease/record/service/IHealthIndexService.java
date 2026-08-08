package com.chronicdisease.record.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.record.domain.dto.HealthIndexDTO;
import com.chronicdisease.record.domain.dto.HealthIndexPageDTO;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import com.chronicdisease.record.domain.vo.TrendPointVO;
import com.chronicdisease.record.domain.vo.AdminDashboardVO;
import java.util.List;
import java.util.Map;

public interface IHealthIndexService extends IService<HealthIndexRecord> {

    void addRecord(HealthIndexDTO dto);

    PageResult<HealthIndexRecord> pageRecords(HealthIndexPageDTO dto);

    void deleteRecord(Long id);

    PageResult<HealthIndexRecord> pageRecordsByPatientName(String patientName, Integer pageNum, Integer pageSize, String indexCode);

    /**
     * 患者端趋势聚合：按日/周/月粒度返回指定指标的聚合数据
     * @param days 查询天数
     * @param granularity DAY / WEEK / MONTH
     * @param indexCodes 指标编码列表，为空则返回全部
     */
    Map<String, List<TrendPointVO>> getTrend(Integer days, String granularity, List<String> indexCodes);

    /**
     * 管理端趋势聚合：查看指定患者的指标趋势，内部通过 Feign 查询 userId
     * @param patientName 患者姓名
     * @param days 查询天数
     * @param granularity DAY / WEEK / MONTH
     * @param indexCodes 指标编码列表
     */
    Map<String, List<TrendPointVO>> getAdminTrend(String patientName, Integer days, String granularity, List<String> indexCodes);

    AdminDashboardVO getDashboard();

    /**
     * 管理端：分页查询所有异常指标记录
     */
    PageResult<HealthIndexRecord> getAbnormalRecords(String patientName, Integer pageNum, Integer pageSize);
}
