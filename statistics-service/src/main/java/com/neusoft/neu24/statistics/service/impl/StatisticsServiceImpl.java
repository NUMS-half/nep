package com.neusoft.neu24.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.client.AqiClient;
import com.neusoft.neu24.client.GridClient;
import com.neusoft.neu24.client.ReportClient;
import com.neusoft.neu24.client.UserClient;
import com.neusoft.neu24.dto.*;
import com.neusoft.neu24.entity.*;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.SaveException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.statistics.mapper.StatisticsMapper;
import com.neusoft.neu24.statistics.service.IStatisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);

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
     * 反馈服务客户端
     */
    private final ReportClient reportClient;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    private StatisticsMapper statisticsMapper;

    /**
     * <b>保存网格员测量的统计信息<b/>
     *
     * @param statistics 待保存的统计信息
     * @return 保存结果
     */
    @Override
    @GlobalTransactional
    public HttpResponseEntity<Statistics> saveStatistics(Statistics statistics) {
        try {
            // 1. 异步校验reportId是否存在
            CompletableFuture<HttpResponseEntity<ReportDTO>> reportFuture = CompletableFuture.supplyAsync(() ->
                    reportClient.selectReportById(statistics.getReportId())
            );

            // 2. 异步校验检测数值合法性
            CompletableFuture<HttpResponseEntity<Boolean>> aqiFuture = CompletableFuture.supplyAsync(() ->
                    aqiClient.validateAqi(statistics)
            );

            // 3. 异步校验网格员是否存在
            CompletableFuture<HttpResponseEntity<User>> userFuture = CompletableFuture.supplyAsync(() ->
                    userClient.selectUser(Map.of("userId", statistics.getGmUserId()))
            );

            // 4. 同步获取所有异步操作的结果
            CompletableFuture.allOf(reportFuture, aqiFuture, userFuture).join();
            HttpResponseEntity<ReportDTO> reportResponse = reportFuture.get();
            HttpResponseEntity<Boolean> aqiResponse = aqiFuture.get();
            HttpResponseEntity<User> userResponse = userFuture.get();

            // 校验reportId是否存在
            if ( reportResponse.getCode() != 200 ) {
                return new HttpResponseEntity<Statistics>().fail(ResponseEnum.REPORT_NOT_EXIST);
            } else {
                ReportDTO report = reportResponse.getData();
                // 校验Report是否已经被确认
                if ( report.getState() == 2 ) {
                    return new HttpResponseEntity<Statistics>().fail(ResponseEnum.REPORT_HAS_CONFIRMED);
                }
                // 校验网格是否与Report的网格对应
                if ( !report.getTownCode().equals(statistics.getTownCode()) ||
                        !report.getCityCode().equals(statistics.getCityCode()) ||
                        !report.getProvinceCode().equals(statistics.getProvinceCode()) ) {
                    return new HttpResponseEntity<Statistics>().fail(ResponseEnum.REGION_INVALID);
                }
                // 校验该用户是否存在
                if ( !report.getUserId().equals(statistics.getUserId()) ) {
                    return new HttpResponseEntity<Statistics>().fail(ResponseEnum.USER_NOT_EXIST);
                }
            }

            // 校验检测数值合法性
            if ( aqiResponse.getCode() != 200 ) {
                return new HttpResponseEntity<Statistics>().fail(ResponseEnum.STATISTICS_VALUE_INVALID);
            }

            // 校验网格员是否存在
            if ( userResponse.getCode() != 200 ) {
                return new HttpResponseEntity<Statistics>().fail(ResponseEnum.USER_NOT_EXIST);
            }


            // 如果所有校验都通过，保存统计信息
            logger.info("数据合法性校验通过，开始保存统计信息");

            statisticsMapper.insert(statistics);
            // 1. 反馈服务更新反馈状态(同步)
            reportClient.setReportState(statistics.getReportId(), 2);
//                // 1. 通知反馈服务更新反馈状态(异步)
//            rabbitTemplate.convertAndSend("statistics.exchange", "save.success", statistics.getReportId());
            // 2. 上报信息更新成功，发送消息到公众监督员的消息队列
            rabbitTemplate.convertAndSend("user.exchange", "notification." + statistics.getUserId(), statistics);
            logger.info("统计信息: {} 保存成功", statistics.getStatisticsId());
            return new HttpResponseEntity<Statistics>().success(statistics);
        } catch ( InterruptedException | ExecutionException e ) {
            logger.error("异步检查输入合法性时发生异常，实测信息: {} 保存失败:{}", statistics.getStatisticsId(), e.getMessage());
            throw new SaveException("异步检查输入合法性时发生异常", e);
        } catch ( DataAccessException e ) {
            logger.warn("数据输入超出数据库限制，实测信息: {} 保存失败:{}", statistics.getStatisticsId(), e.getMessage());
            throw new SaveException("保存实测信息时,数据输入超出数据库限制", e);
        } catch ( AmqpException e ) {
            logger.warn("AMQP 异常:{}, 实测信息保存失败", e.getMessage());
            throw new SaveException("保存实测信息时,AMQP发生异常", e);
        } catch ( Exception e ) {
            logger.error("保存实测信息: {} 时发生异常: {}", statistics.getStatisticsId(), e.getMessage());
            throw new SaveException("保存实测信息时发生异常", e);
        }
    }

    /**
     * <b>更新统计信息<b/>
     *
     * @param statistics 待更新的统计信息
     * @return 更新结果
     */
    @Override
    @Transactional
    public HttpResponseEntity<Boolean> updateStatistics(Statistics statistics) {
        try {
            return statisticsMapper.updateById(statistics) != 0 ?
                    new HttpResponseEntity<Boolean>().success(true) :
                    new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( DataAccessException e ) {
            logger.warn("更新统计信息失败:DataAccessException {}", e.getMessage());
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            logger.error("统计信息更新失败:{}", e.getMessage(), e);
            throw new UpdateException("更新统计信息失败", e);
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
    @Transactional(readOnly = true)
    public HttpResponseEntity<IPage<StatisticsDTO>> selectStatisticsByPage(Statistics statistics, long current, long size) {
        try {
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
        } catch ( Exception e ) {
            logger.error("分页查询统计信息失败:{}", e.getMessage());
            throw new QueryException("分页查询统计信息时发生异常", e);
        }
    }

    /**
     * <b>根据ID查询统计信息<b/>
     *
     * @param statisticsId 统计信息ID
     * @return 查询结果
     */
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<ItemizedStatisticsDTO>> selectItemizedStatistics(String provinceCode) {
        List<ItemizedStatisticsDTO> list;
        if ( provinceCode == null || provinceCode.isEmpty() ) {
            list = statisticsMapper.selectItemizedStatistics(null);
            Map<Object, Object> provinceMap = gridClient.selectProvinceMap().getData();
            list.forEach(item -> item.setProvinceName((String) provinceMap.get(item.getProvinceCode())));
        } else {
            list = statisticsMapper.selectItemizedStatistics(provinceCode);
            Map<Object, Object> cityMap = gridClient.selectCitiesByProvinceCode(provinceCode).getData();
            list.forEach(item -> item.setCityName((String) cityMap.get(item.getCityCode())));
        }
        if ( list.isEmpty() ) {
            return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().resultIsNull(null);
        }
        return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().success(list);
    }

    /**
     * 按月查询AQI指数超标统计
     *
     * @return AQI指数等级分布统计
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<MonthAQIExcessDTO>> selectAQIExcessTendency() {
        List<MonthAQIExcessDTO> list = statisticsMapper.selectMonthAQIExcess();
        if ( list == null || list.isEmpty() ) {
            return new HttpResponseEntity<List<MonthAQIExcessDTO>>().resultIsNull(null);
        }
        return new HttpResponseEntity<List<MonthAQIExcessDTO>>().success(list);
    }

    /**
     * AQI指数等级分布统计
     *
     * @return AQI指数等级分布统计列表
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<AQIDistributeDTO>> selectAQIDistribution() {
        List<AQIDistributeDTO> list = statisticsMapper.selectAQIDistribution();
        if ( list == null || list.isEmpty() ) {
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
     * 查询统计信息总览
     *
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<StatisticsTotalDTO> selectStatisticsSummary() {
        StatisticsTotalDTO totalDTO = statisticsMapper.selectStatisticsSummary();
        Map<Object, Object> gridTotal = gridClient.selectGridTotal().getData();
        totalDTO.setProvince((Integer) gridTotal.get("province"));
        totalDTO.setCity((Integer) gridTotal.get("city"));
        totalDTO.setTown((Integer) gridTotal.get("town"));
        return new HttpResponseEntity<StatisticsTotalDTO>().success(totalDTO);
    }

    /**
     * 根据统计信息填充StatisticsDTO
     */
    private StatisticsDTO fillStatisticsDTO(Statistics statistics) {
        // 1. 异步调用服务
        CompletableFuture<HttpResponseEntity<User>> userFuture = CompletableFuture.supplyAsync(() ->
                userClient.selectUser(Map.of("userId", statistics.getUserId()))
        );
        CompletableFuture<HttpResponseEntity<User>> gmFuture = CompletableFuture.supplyAsync(() ->
                userClient.selectUser(Map.of("userId", statistics.getGmUserId()))
        );
        CompletableFuture<HttpResponseEntity<Grid>> gridFuture = CompletableFuture.supplyAsync(() ->
                gridClient.selectGridByTownCode(statistics.getTownCode())
        );

        // 2. 等待所有调用完成
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
        } catch ( InterruptedException | ExecutionException e ) {
            logger.error(e.getMessage());
            return null;
        }
    }

}
