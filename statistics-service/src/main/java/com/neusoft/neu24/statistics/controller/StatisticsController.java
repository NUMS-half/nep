package com.neusoft.neu24.statistics.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.*;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.SaveException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.statistics.service.IStatisticsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <b>检测统计信息前端控制器</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    /**
     * 检测统计信息业务层接口
     */
    @Resource
    IStatisticsService statisticsService;

    /**
     * 保存网格员测量的统计信息
     *
     * @param map 待保存的统计信息
     * @return 保存结果
     */
    @PostMapping(value = "/save", headers = "Accept=application/json")
    public HttpResponseEntity<Statistics> saveStatistics(@RequestBody Map<String, Object> map) {
        try {
            // 1. 将参数封装为Statistics对象
            Statistics statistics = BeanUtil.fillBeanWithMap(map, new Statistics(), false);
            // 2. 保存统计信息
            return statisticsService.saveStatistics(statistics);
        } catch ( SaveException e ) {
            // 3. 保存异常处理
            logger.error("保存统计信息时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Statistics>().serverError(null);
        } catch ( Exception e ) {
            // 4. 未知异常处理
            logger.error("保存统计信息时发生未知异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Statistics>().serverError(null);
        }
    }

    /**
     * 根据ID查询统计信息
     * @param statisticsId 统计信息ID
     * @return 查询结果
     */
    @GetMapping(value = "/select")
    public HttpResponseEntity<StatisticsDTO> selectStatisticsById(@RequestParam("statisticsId") String statisticsId) {
        try {
            // 1. 查询统计信息
            return statisticsService.selectStatisticsById(statisticsId);
        } catch ( QueryException e ) {
            // 2. 查询异常处理
            logger.error("查询统计信息时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<StatisticsDTO>().serverError(null);
        }
    }

    @GetMapping(value = "/select/report")
    public HttpResponseEntity<List<Statistics>> selectStatisticsByReportId(@RequestParam("reportId") String reportId) {
        try {
            // 1. 查询统计信息
            return statisticsService.selectStatisticsByReportId(reportId);
        } catch ( QueryException e ) {
            // 2. 查询异常处理
            logger.error("查询统计信息时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<List<Statistics>>().serverError(null);
        }
    }

    /**
     * 条件分页查询统计信息
     *
     * @param map     查询条件(为null时查询全部)
     * @param current 当前页
     * @param size    每页数据条数
     * @return 分页查询结果
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<StatisticsDTO>> selectStatisticsByPage(@RequestBody Map<String, Object> map, @RequestParam("current") long current, @RequestParam("size") long size) {
        try {
            // 1. 查询条件为空时查询全部
            if ( map == null ) {
                return statisticsService.selectStatisticsByPage(null, current, size);
            }
            // 2. 查询条件不为空时根据条件查询
            else {
                // 2.1 将Map转换为Statistics对象
                Statistics statistics = BeanUtil.fillBeanWithMap(map, new Statistics(), false);
                return statisticsService.selectStatisticsByPage(statistics, current, size);
            }
        } catch ( QueryException e ) {
            // 3. 查询异常处理
            logger.error("条件分页查询统计信息时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<IPage<StatisticsDTO>>().serverError(null);
        }
    }

    /**
     * 更新统计信息
     *
     * @param map 待更新的统计信息
     * @return 更新结果
     */
    @PutMapping(value = "/update", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateStatistics(@RequestBody Map<String, Object> map)   {
        try {
            // 1. 将参数封装为Statistics对象
            Statistics statistics = BeanUtil.fillBeanWithMap(map, new Statistics(), false);
            // 2. 更新统计信息
            return statisticsService.updateStatistics(statistics);
        } catch ( UpdateException e ) {
            // 3. 更新异常处理
            logger.error("更新统计信息时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Boolean>().serverError(false);
        }
    }

    /**
     * 查询省/市分项指标超标统计
     * @param provinceCode 省/市编码
     *                     为空时查询全国
     *                     不为空时查询省/市分项指标超标统计
     * @return 查询结果
     */
    @GetMapping(value = "/excess/item")
    public HttpResponseEntity<List<ItemizedStatisticsDTO>> getItemExcess(@RequestParam(value = "provinceCode",required = false) String provinceCode) {
        try {
            return statisticsService.selectItemizedStatistics(provinceCode);
        } catch ( QueryException e ) {
            return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().serverError(null);
        }
    }

    /**
     * 按月查询所有AQI指数超标趋势统计
     * @return AQI指数超标趋势统计
     */
    @GetMapping(value = "/excess/tendency/all")
    public HttpResponseEntity<List<MonthAQIExcessDTO>> getMonthTendency() {
        try {
            // 1. 查询所有月份AQI指数超标统计
            return statisticsService.selectAQIExcessTendency();
        } catch ( QueryException e ) {
            // 2. 查询异常处理
            logger.error("查询所有月份AQI指数超标统计时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<List<MonthAQIExcessDTO>>().serverError(null);
        }
    }

    /**
     * 按月分页查询AQI指数超标趋势统计
     * @param current 当前页
     * @param size 每页数据条数
     * @return AQI指数超标趋势统计
     */
    @GetMapping(value = "/excess/tendency")
    public HttpResponseEntity<IPage<MonthAQIExcessDTO>> getMonthAQIExcessPage(@RequestParam("current") int current, @RequestParam("size") int size) {
        try {
            // 1. 分页查询AQI指数超标统计
            return statisticsService.selectAQIExcessTendencyPage(current, size);
        } catch ( QueryException e ) {
            // 2. 查询异常处理
            logger.error("分页查询AQI指数超标统计时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<IPage<MonthAQIExcessDTO>>().serverError(null);
        }
    }

    /**
     * AQI指数等级分布统计
     * @return AQI指数等级分布统计
     */
    @GetMapping(value = "/aqi/distribute")
    public HttpResponseEntity<List<AQIDistributeDTO>> getAQIDistribute() {
        try {
            // 1. 查询AQI指数等级分布统计
            return statisticsService.selectAQIDistribution();
        } catch ( QueryException e ) {
            // 2. 查询异常处理
            logger.error("查询AQI指数等级分布统计时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<List<AQIDistributeDTO>>().serverError(null);
        }
    }

    /**
     * 查询全国统计信息汇总
     * @return 全国统计信息汇总
     */
    @GetMapping(value = "/summary")
    public HttpResponseEntity<Map<String,StatisticsTotalDTO>> getStatisticsSummary() {
        try {
            // 1. 查询全国统计信息汇总
            return statisticsService.selectStatisticsSummary();
        } catch ( QueryException e ) {
            // 2. 查询异常处理
            logger.error("查询全国统计信息汇总时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Map<String,StatisticsTotalDTO>>().serverError(null);
        }
    }
}

