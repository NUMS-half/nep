package com.neusoft.neu24.statistics.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.*;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Statistics;
import com.neusoft.neu24.statistics.service.IAqiService;
import com.neusoft.neu24.statistics.service.IStatisticsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Resource
    IStatisticsService statisticsService;

    /**
     * <b>保存网格员测量的统计信息<b/>
     *
     * @param map 待保存的统计信息
     * @return 保存结果
     */
    @PostMapping(value = "/save", headers = "Accept=application/json")
    public HttpResponseEntity<Statistics> saveStatistics(@RequestBody Map<String, Object> map) {
        try {
            // 将Map转换为Statistics对象
            Statistics statistics = BeanUtil.fillBeanWithMap(map, new Statistics(), false);
            // 设置统计的确认时间
            statistics.setConfirmTime(LocalDateTimeUtil.now());
            return statisticsService.saveStatistics(statistics);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Statistics>().serverError(null);
        }
    }

    /**
     * <b>根据ID查询统计信息<b/>
     * @param statisticsId 统计信息ID
     * @return 查询结果
     */
    @GetMapping(value = "/select/{statisticsId}")
    public HttpResponseEntity<StatisticsDTO> selectStatisticsById(@PathVariable("statisticsId") String statisticsId) {
        return statisticsService.selectStatisticsById(statisticsId);
    }

    /**
     * <b>条件分页查询统计信息<b/>
     *
     * @param map     查询条件(为null时查询全部)
     * @param current 当前页
     * @param size    每页数据条数
     * @return 分页查询结果
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<StatisticsDTO>> selectStatisticsByPage(@RequestBody Map<String, Object> map, @RequestParam("current") long current, @RequestParam("size") long size) {
        try {
            if ( map == null ) {
                return statisticsService.selectStatisticsByPage(null, current, size);
            } else {
                // 将Map转换为Statistics对象
                Statistics statistics = BeanUtil.fillBeanWithMap(map, new Statistics(), false);
                return statisticsService.selectStatisticsByPage(statistics, current, size);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<IPage<StatisticsDTO>>().serverError(null);
        }
    }

    /**
     * <b>更新统计信息<b/>
     *
     * @param map 待更新的统计信息
     * @return 更新结果
     */
    @PutMapping(value = "/update", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> updateStatistics(@RequestBody Map<String, Object> map) {
        try {
            // 将Map转换为Statistics对象
            Statistics statistics = BeanUtil.fillBeanWithMap(map, new Statistics(), false);
            return statisticsService.updateStatistics(statistics);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(false);
        }
    }

    /**
     * 查询省/市分项指标超标统计
     */
    @GetMapping(value = "/excess/item")
    public HttpResponseEntity<List<ItemizedStatisticsDTO>> getItemExcess(@RequestParam(value = "provinceCode",required = false) String provinceCode) {
//        try {
            return statisticsService.selectItemizedStatistics(provinceCode);
//        } catch ( Exception e ) {
//            return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().serverError(null);
//        }
    }

    /**
     * 按月查询AQI指数超标统计
     */
    @GetMapping(value = "/excess/tendency")
    public HttpResponseEntity<List<MonthAQIExcessDTO>> getMonthAQIExcess(@RequestParam(value = "provinceCode",required = false) String provinceCode) {
        try {
            return statisticsService.selectAQIExcessTendency();
        } catch ( Exception e ) {
            return new HttpResponseEntity<List<MonthAQIExcessDTO>>().serverError(null);
        }
    }

    /**
     * AQI指数等级分布统计
     */
    @GetMapping(value = "/aqi/distribute")
    public HttpResponseEntity<List<AQIDistributeDTO>> getAQIDistribute() {
        try {
            return statisticsService.selectAQIDistribution();
        } catch ( Exception e ) {
            return new HttpResponseEntity<List<AQIDistributeDTO>>().serverError(null);
        }
    }

    @GetMapping(value = "/summary")
    public HttpResponseEntity<StatisticsTotalDTO> getStatisticsSummary() {
        try {
            return statisticsService.selectStatisticsSummary();
        } catch ( Exception e ) {
            return new HttpResponseEntity<StatisticsTotalDTO>().serverError(null);
        }
    }
}

