package com.chronicdisease.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronicdisease.record.domain.entity.HealthIndexRecord;
import com.chronicdisease.record.domain.vo.DailyAggregation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthIndexMapper extends BaseMapper<HealthIndexRecord> {

    /**
     * 按天聚合指标数据：AVG / MAX / MIN / COUNT
     * 利用 idx_user_index_time 联合索引，性能高效
     */
    @Select("<script>" +
            "SELECT index_code, DATE(record_time) AS record_date, " +
            "AVG(index_value) AS avg_value, MAX(index_value) AS max_value, " +
            "MIN(index_value) AS min_value, COUNT(*) AS record_count " +
            "FROM health_index_record " +
            "WHERE user_id = #{userId} AND is_deleted = 0 " +
            "AND record_time BETWEEN #{startTime} AND #{endTime} " +
            "<if test='indexCodes != null and indexCodes.size() > 0'>" +
            "AND index_code IN " +
            "<foreach collection='indexCodes' item='code' open='(' separator=',' close=')'>#{code}</foreach>" +
            "</if>" +
            "GROUP BY index_code, DATE(record_time) " +
            "ORDER BY record_date ASC" +
            "</script>")
    List<DailyAggregation> dailyAggregation(@Param("userId") Long userId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            @Param("indexCodes") List<String> indexCodes);
}
