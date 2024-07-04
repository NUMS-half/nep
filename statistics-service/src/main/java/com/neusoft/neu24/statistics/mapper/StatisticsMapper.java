package com.neusoft.neu24.statistics.mapper;

import com.neusoft.neu24.dto.AQIDistributeDTO;
import com.neusoft.neu24.dto.ItemizedStatisticsDTO;
import com.neusoft.neu24.dto.MonthAQIExcessDTO;
import com.neusoft.neu24.dto.StatisticsTotalDTO;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.Statistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Mapper
public interface StatisticsMapper extends BaseMapper<Statistics> {

    /**
     * 查询省/市分项指标超标统计
     * @param provinceCode 省份编码(为空时按省分，不为空时按市分)
     * @return 分项指标超标统计
     */
    List<ItemizedStatisticsDTO> selectItemizedStatistics(@Param("provinceCode") String provinceCode);

    /**
     * 按月查询AQI指数超标统计
     * @return AQI指数等级分布统计
     */
    List<MonthAQIExcessDTO> selectMonthAQIExcess();

    /**
     * AQI指数等级分布统计
     * @return AQI指数等级分布统计列表
     */
    List<AQIDistributeDTO> selectAQIDistribution();

    /**
     * 查询总计信息
     * @return 总计信息
     */
    @MapKey("total")
    StatisticsTotalDTO selectStatisticsSummary(@Param("time")LocalDateTime time);
}
