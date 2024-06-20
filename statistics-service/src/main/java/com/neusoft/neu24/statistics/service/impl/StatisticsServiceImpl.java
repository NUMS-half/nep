package com.neusoft.neu24.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.client.AqiClient;
import com.neusoft.neu24.client.GridClient;
import com.neusoft.neu24.client.UserClient;
import com.neusoft.neu24.dto.*;
import com.neusoft.neu24.entity.*;
import com.neusoft.neu24.statistics.mapper.StatisticsMapper;
import com.neusoft.neu24.statistics.service.IStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * <b>统计信息服务实现类</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl extends ServiceImpl<StatisticsMapper, Statistics> implements IStatisticsService {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(StatisticsServiceImpl.class);

    @Resource
    private StatisticsMapper statisticsMapper;

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 用户服务客户端
     */
    private final UserClient userClient;

    /**
     * 网格服务客户端
     */
    private final GridClient gridClient;

    /**
     * AQI客户端
     */
    private final AqiClient aqiClient;

    /**
     * <b>保存网格员测量的统计信息<b/>
     *
     * @param statistics 待保存的统计信息
     * @return 保存结果
     */
    @Override
    public HttpResponseEntity<Statistics> saveStatistics(Statistics statistics) {
        try {
            if ( statisticsMapper.insert(statistics) != 0 ) {
                // 1. 通知反馈服务更新反馈状态
                rabbitTemplate.convertAndSend("statistics.exchange", "save.success", statistics.getReportId());
                // 2. 上报信息更新成功，发送消息到公众监督员的消息队列
                rabbitTemplate.convertAndSend("user.exchange", "notification." + statistics.getUserId(), statistics);
                return new HttpResponseEntity<Statistics>().success(statistics);
            } else {
                return new HttpResponseEntity<Statistics>().addFail(null);
            }
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Statistics>().addFail(null);
        } catch ( AmqpException e ) {
            return new HttpResponseEntity<Statistics>().serviceUnavailable(null);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Statistics>().serverError(null);
        }
    }

    /**
     * <b>更新统计信息<b/>
     *
     * @param statistics 待更新的统计信息
     * @return 更新结果
     */
    @Override
    public HttpResponseEntity<Boolean> updateStatistics(Statistics statistics) {
        try {
            return statisticsMapper.updateById(statistics) != 0 ?
                    new HttpResponseEntity<Boolean>().success(true) :
                    HttpResponseEntity.UPDATE_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.UPDATE_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(false);
        }
    }


    /**
     * <b>条件分页查询统计信息<b/>
     *
     * @param statistics 查询条件
     * @param current    当前页
     * @param size       每页数据条数
     * @return 分页查询结果
     */
    @Override
    public HttpResponseEntity<IPage<StatisticsDTO>> selectStatisticsByPage(Statistics statistics, long current, long size) {
        IPage<Statistics> page = new Page<>(current, size);
        IPage<Statistics> pages;
        LambdaQueryWrapper<Statistics> queryWrapper = new LambdaQueryWrapper<>();
        // 可通过省市区和确认时间四种条件查询
        if ( statistics != null ) {
            queryWrapper.eq(statistics.getProvinceCode() != null, Statistics::getProvinceCode, statistics.getProvinceCode())
                    .eq(statistics.getCityCode() != null, Statistics::getCityCode, statistics.getCityCode())
                    .eq(statistics.getTownCode() != null, Statistics::getTownCode, statistics.getTownCode())
                    .eq(statistics.getConfirmTime() != null, Statistics::getConfirmTime, statistics.getConfirmTime());
            pages = getBaseMapper().selectPage(page, queryWrapper);
        } else {
            pages = getBaseMapper().selectPage(page, null);
        }
        if ( pages == null || pages.getTotal() == 0 ) {
            return new HttpResponseEntity<IPage<StatisticsDTO>>().resultIsNull(null);
        }
        IPage<StatisticsDTO> dtoPages = pages.convert(this::fillStatisticsDTO);
        return new HttpResponseEntity<IPage<StatisticsDTO>>().success(dtoPages);
    }

    /**
     * <b>根据ID查询统计信息<b/>
     *
     * @param statisticsId 统计信息ID
     * @return 查询结果
     */
    @Override
    public HttpResponseEntity<StatisticsDTO> selectStatisticsById(String statisticsId) {
        if ( statisticsId == null ) {
            return new HttpResponseEntity<StatisticsDTO>().resultIsNull(null);
        } else {
            QueryWrapper<Statistics> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("statistics_id", statisticsId);
            Statistics result = statisticsMapper.selectOne(queryWrapper);
            if ( result == null ) {
                return new HttpResponseEntity<StatisticsDTO>().resultIsNull(null);
            } else {
                StatisticsDTO statisticsDTO = fillStatisticsDTO(result);
                return new HttpResponseEntity<StatisticsDTO>().success(statisticsDTO);
            }
        }
    }

    /**
     * 查询省/市分项指标超标统计
     *
     * @param provinceCode 省份编码(为空时按省分，不为空时按市分)
     * @return 分项指标超标统计
     */
    @Override
    public HttpResponseEntity<List<ItemizedStatisticsDTO>> selectItemizedStatistics(String provinceCode) {
        List<ItemizedStatisticsDTO> list;
        if ( provinceCode == null || provinceCode.isEmpty() ) {
            list = statisticsMapper.selectItemizedStatistics(null);
            Map<Object,Object> provinceMap = gridClient.selectProvinceMap().getData();
            list.forEach(item -> item.setProvinceName((String) provinceMap.get(item.getProvinceCode())));
        } else {
            list = statisticsMapper.selectItemizedStatistics(provinceCode);
            Map<Object,Object> cityMap = gridClient.selectCitiesByProvinceCode(provinceCode).getData();
            list.forEach(item -> item.setCityName((String) cityMap.get(item.getCityCode())));
        }
        if ( list.isEmpty() ) {
            return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().resultIsNull(null);
        }
        return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().success(list);
    }

    /**
     * 按月查询AQI指数超标统计
     * @return AQI指数等级分布统计
     */
    @Override
    public HttpResponseEntity<List<MonthAQIExcessDTO>> selectAQIExcessTendency() {
        List<MonthAQIExcessDTO> list = statisticsMapper.selectMonthAQIExcess();
        if( list == null || list.isEmpty() ) {
            return new HttpResponseEntity<List<MonthAQIExcessDTO>>().resultIsNull(null);
        }
        return new HttpResponseEntity<List<MonthAQIExcessDTO>>().success(list);
    }

    /**
     * AQI指数等级分布统计
     * @return AQI指数等级分布统计列表
     */
    @Override
    public HttpResponseEntity<List<AQIDistributeDTO>> selectAQIDistribution() {
        List<AQIDistributeDTO> list = statisticsMapper.selectAQIDistribution();
        if( list == null || list.isEmpty() ) {
            return new HttpResponseEntity<List<AQIDistributeDTO>>().resultIsNull(null);
        }
        List<Aqi> aqiList = aqiClient.selectAllAqi().getData();
        list.forEach(item -> {
            Aqi aqi = aqiList.stream().filter(a -> a.getAqiId().equals(item.getAqiId())).findFirst().orElse(null);
            if ( aqi != null ) {
                item.setAqiLevel(aqi.getAqiLevel());
                item.setAqiExplain(aqi.getAqiExplain());
            }
        });
        return new HttpResponseEntity<List<AQIDistributeDTO>>().success(list);
    }

    /**
     * @return
     */
    @Override
    public HttpResponseEntity<StatisticsTotalDTO> selectStatisticsSummary() {
        StatisticsTotalDTO totalDTO = statisticsMapper.selectStatisticsSummary();
        Map<Object,Object> gridTotal = gridClient.selectGridTotal().getData();
        totalDTO.setProvince((Integer) gridTotal.get("province"));
        totalDTO.setCity((Integer) gridTotal.get("city"));
        totalDTO.setTown((Integer) gridTotal.get("town"));
        return new HttpResponseEntity<StatisticsTotalDTO>().success(totalDTO);
    }

    private StatisticsDTO fillStatisticsDTO(Statistics statistics) {
        // 异步调用服务
        CompletableFuture<HttpResponseEntity<User>> userFuture = CompletableFuture.supplyAsync(() ->
                userClient.selectUser(Map.of("userId", statistics.getUserId()))
        );
        CompletableFuture<HttpResponseEntity<User>> gmFuture = CompletableFuture.supplyAsync(() ->
                userClient.selectUser(Map.of("userId", statistics.getGmUserId()))
        );
        CompletableFuture<HttpResponseEntity<Grid>> gridFuture = CompletableFuture.supplyAsync(() ->
                gridClient.selectGridByTownCode(statistics.getTownCode())
        );

        // 等待所有调用完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(userFuture, gmFuture, gridFuture);

        try {
            allFutures.join(); // 等待所有异步任务完成

            HttpResponseEntity<User> userResponse = userFuture.get();
            HttpResponseEntity<User> gmResponse = gmFuture.get();
            HttpResponseEntity<Grid> gridResponse = gridFuture.get();

            if ( userResponse.getCode() != 200 || gmResponse.getCode() != 200 || gridResponse.getCode() != 200 ) {
                return null;
            }

            StatisticsDTO statisticsDTO = new StatisticsDTO(statistics);
            statisticsDTO.fillUserInfo(userResponse.getData(), gmResponse.getData());
            statisticsDTO.fillGridInfo(gridResponse.getData());
            return statisticsDTO;
        } catch ( InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

}
