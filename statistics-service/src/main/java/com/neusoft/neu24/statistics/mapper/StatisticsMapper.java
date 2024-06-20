package com.neusoft.neu24.statistics.mapper;

import com.neusoft.neu24.dto.AQIDistributeDTO;
import com.neusoft.neu24.dto.ItemizedStatisticsDTO;
import com.neusoft.neu24.dto.MonthAQIExcessDTO;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.Statistics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

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
     * @param provinceCode 省份编码(为空查全部)
     * @return AQI指数等级分布统计
     */
    List<MonthAQIExcessDTO> selectMonthAQIExcess(@Param("provinceCode") String provinceCode);

    /**
     * AQI指数等级分布统计
     * @param provinceCode 省份编码(为空查全部)
     * @return AQI指数等级分布统计列表
     */
    List<AQIDistributeDTO> selectAQIDistribution(@Param("provinceCode") String provinceCode);
}
