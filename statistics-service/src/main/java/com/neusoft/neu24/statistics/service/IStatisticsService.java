package com.neusoft.neu24.statistics.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.*;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <b>检测统计信息服务接口</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IStatisticsService extends IService<Statistics> {


    /**
     * 保存网格员测量的统计信息
     * @param statistics 待保存的统计信息
     * @return 保存结果
     */
    HttpResponseEntity<Statistics> saveStatistics(Statistics statistics);


    /**
     * 更新统计信息
     * @param statistics 待更新的统计信息
     * @return 更新结果
     */
    HttpResponseEntity<Boolean> updateStatistics(Statistics statistics);

    /**
     * 条件分页查询统计信息
     * @param statistics 查询条件
     * @param current 当前页
     * @param size 每页数据条数
     * @return 分页查询结果
     */
    HttpResponseEntity<IPage<StatisticsDTO>> selectStatisticsByPage(Statistics statistics, long current, long size);

    /**
     * 根据ID查询统计信息
     * @param statisticsId 统计信息ID
     * @return 查询结果
     */
    HttpResponseEntity<StatisticsDTO> selectStatisticsById(String statisticsId);

    /**
     * 根据反馈ID查询统计信息
     *
     * @param reportId 反馈ID
     * @return 查询结果
     */
    HttpResponseEntity<List<Statistics>> selectStatisticsByReportId(String reportId);

    /**
     * 查询省/市分项指标超标统计
     * @param provinceCode 省份编码(为空时按省分，不为空时按市分)
     * @return 分项指标超标统计
     */
    HttpResponseEntity<List<ItemizedStatisticsDTO>> selectItemizedStatistics(String provinceCode);

    /**
     * 按月查询所有AQI指数超标统计
     * @return AQI超标趋势统计数据
     */
    HttpResponseEntity<List<MonthAQIExcessDTO>> selectAQIExcessTendency();

    /**
     * 按月分页查询AQI指数超标统计
     * @param current 当前页
     * @param size 每页数据条数
     * @return AQI超标趋势统计
     */
    HttpResponseEntity<IPage<MonthAQIExcessDTO>> selectAQIExcessTendencyPage(int current, int size);

    /**
     * AQI指数等级分布统计
     * @return AQI指数等级分布统计列表
     */
    HttpResponseEntity<List<AQIDistributeDTO>> selectAQIDistribution();

    /**
     * 查询全国统计信息汇总
     * @return 全国统计信息汇总Map
     */
    HttpResponseEntity<Map<String,StatisticsTotalDTO>> selectStatisticsSummary();
}
