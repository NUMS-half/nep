package com.neusoft.neu24.statistics.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            logger.error("异步检查输入合法性时发生异常，实测信息: {} 保存失败:{}", statistics.getStatisticsId(), e.getMessage(), e);
            throw new SaveException("异步检查输入合法性时发生异常", e);
        } catch ( DataAccessException e ) {
            logger.warn("数据输不符合数据库约束，实测信息: {} 保存失败:{}", statistics.getStatisticsId(), e.getMessage(), e);
            throw new SaveException("保存实测信息时,数据输入不符合数据库约束", e);
        } catch ( AmqpException e ) {
            logger.warn("AMQP 异常:{}, 实测信息保存失败", e.getMessage(), e);
            throw new SaveException("保存实测信息时,AMQP发生异常", e);
        } catch ( Exception e ) {
            logger.error("保存实测信息: {} 时发生异常: {}", statistics.getStatisticsId(), e.getMessage(), e);
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
            logger.warn("更新统计信息失败 {}", e.getMessage(), e);
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
                queryWrapper.eq(StringUtils.isNotBlank(statistics.getGmUserId()), Statistics::getGmUserId, statistics.getGmUserId())
                        .eq(StringUtils.isNotBlank(statistics.getProvinceCode()), Statistics::getProvinceCode, statistics.getProvinceCode())
                        .eq(StringUtils.isNotBlank(statistics.getCityCode()), Statistics::getCityCode, statistics.getCityCode())
                        .eq(StringUtils.isNotBlank(statistics.getTownCode()), Statistics::getTownCode, statistics.getTownCode())
                        .eq(statistics.getConfirmTime() != null, Statistics::getConfirmTime, statistics.getConfirmTime())
                        .eq(statistics.getAqiId() != null, Statistics::getAqiId, statistics.getAqiId());
                pages = getBaseMapper().selectPage(page, queryWrapper);
            } else {
                pages = getBaseMapper().selectPage(page, null);
            }
            if ( pages == null || pages.getTotal() == 0 ) {
                return new HttpResponseEntity<IPage<StatisticsDTO>>().resultIsNull(null);
            }
            IPage<StatisticsDTO> dtoPages = new Page<>();
            dtoPages.setRecords(fillStatisticsDTO(pages.getRecords()));
            dtoPages.setTotal(pages.getTotal());
            dtoPages.setCurrent(pages.getCurrent());
            dtoPages.setSize(pages.getSize());
            return new HttpResponseEntity<IPage<StatisticsDTO>>().success(dtoPages);
        } catch ( Exception e ) {
            logger.error("分页查询统计信息失败:{}", e.getMessage(), e);
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
    public HttpResponseEntity<StatisticsDTO> selectStatisticsById(String statisticsId) throws QueryException {
        if ( StringUtils.isEmpty(statisticsId) ) {
            return new HttpResponseEntity<StatisticsDTO>().fail(ResponseEnum.CONTENT_IS_NULL);
        } else {
            try {
                QueryWrapper<Statistics> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("statistics_id", statisticsId);
                Statistics result = statisticsMapper.selectOne(queryWrapper);
                if ( result == null ) {
                    return new HttpResponseEntity<StatisticsDTO>().resultIsNull(null);
                } else {
                    StatisticsDTO statisticsDTO = fillStatisticsDTO(result);
                    return new HttpResponseEntity<StatisticsDTO>().success(statisticsDTO);
                }
            } catch ( QueryException e ) {
                logger.error("根据ID查询统计信息失败:{}", e.getMessage(), e);
                throw new QueryException("根据ID查询统计信息时发生异常", e);
            }
        }
    }

    /**
     * <b>根据反馈ID查询统计信息<b/>
     *
     * @param reportId 反馈ID
     * @return 查询结果
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<Statistics>> selectStatisticsByReportId(String reportId) {
        if ( StringUtils.isEmpty(reportId) ) {
            return new HttpResponseEntity<List<Statistics>>().fail(ResponseEnum.CONTENT_IS_NULL);
        } else {
            try {
                LambdaQueryWrapper<Statistics> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Statistics::getReportId, reportId).orderByDesc(Statistics::getConfirmTime);
                List<Statistics> list = statisticsMapper.selectList(queryWrapper);
                if ( list.isEmpty() ) {
                    return new HttpResponseEntity<List<Statistics>>().resultIsNull(null);
                } else {
                    logger.info("根据反馈ID: {} 查询统计信息成功", reportId);
                    return new HttpResponseEntity<List<Statistics>>().success(list);
                }
            } catch ( QueryException e ) {
                logger.error("根据反馈ID查询统计信息失败:{}", e.getMessage(), e);
                throw new QueryException("根据反馈ID查询统计信息时发生异常", e);
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
        try {
            // 1. 省份编码为空，进行省分组分项查询
            if ( StringUtils.isEmpty(provinceCode) ) {
                list = statisticsMapper.selectItemizedStatistics(null);
                Map<Object, Object> provinceMap = gridClient.selectProvinceMap().getData();
                list.forEach(item -> item.setProvinceName((String) provinceMap.get(item.getProvinceCode())));
            }
            // 2. 省份编码不为空，进行市分组分项查询
            else {
                list = statisticsMapper.selectItemizedStatistics(provinceCode);
                Map<Object, Object> cityMap = gridClient.selectCitiesByProvinceCode(provinceCode).getData();
                list.forEach(item -> item.setCityName((String) cityMap.get(item.getCityCode())));
            }
            if ( list.isEmpty() ) {
                return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().resultIsNull(null);
            }
            logger.info("查询省/市分项指标超标统计成功");
            return new HttpResponseEntity<List<ItemizedStatisticsDTO>>().success(list);
        } catch ( Exception e ) {
            logger.error("查询省/市分项指标超标统计失败:{}", e.getMessage(), e);
            throw new QueryException("查询省/市分项指标超标统计时发生异常", e);
        }
    }

    /**
     * 按月查询所有AQI指数超标统计
     *
     * @return AQI超标趋势统计数据
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<MonthAQIExcessDTO>> selectAQIExcessTendency() {
        List<MonthAQIExcessDTO> list = statisticsMapper.selectMonthAQIExcess();
        if ( CollUtil.isEmpty(list) ) {
            return new HttpResponseEntity<List<MonthAQIExcessDTO>>().resultIsNull(null);
        }
        logger.info("查询按月AQI指数超标统计成功");
        return new HttpResponseEntity<List<MonthAQIExcessDTO>>().success(list);
    }

    /**
     * 按月分页查询AQI指数超标统计
     *
     * @return AQI指数等级分布统计
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<IPage<MonthAQIExcessDTO>> selectAQIExcessTendencyPage(int current, int size) {
        try {
            int offset = (current - 1) * size;
            List<MonthAQIExcessDTO> list = statisticsMapper.selectMonthAQIExcess();
            if ( CollUtil.isEmpty(list) ) {
                return new HttpResponseEntity<IPage<MonthAQIExcessDTO>>().resultIsNull(null);
            }

            // 实现分页
            List<MonthAQIExcessDTO> paginatedList = list.stream()
                    .skip(offset)
                    .limit(size)
                    .collect(Collectors.toList());

            IPage<MonthAQIExcessDTO> page = new Page<>(current, size, list.size());
            page.setRecords(paginatedList);

            if ( CollUtil.isEmpty(paginatedList) ) {
                return new HttpResponseEntity<IPage<MonthAQIExcessDTO>>().resultIsNull(null);
            }
            logger.info("分页查询按月AQI指数超标统计成功");
            return new HttpResponseEntity<IPage<MonthAQIExcessDTO>>().success(page);
        } catch ( Exception e ) {
            logger.error("查询按月AQI指数超标统计失败:{}", e.getMessage(), e);
            throw new QueryException("查询按月AQI指数超标统计时发生异常", e);
        }
    }

    /**
     * AQI指数等级分布统计
     *
     * @return AQI指数等级分布统计列表
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<List<AQIDistributeDTO>> selectAQIDistribution() {
        try {
            // 1. 查询AQI分布情况
            List<AQIDistributeDTO> list = statisticsMapper.selectAQIDistribution();
            int total = list.stream().mapToInt(AQIDistributeDTO::getCount).sum();
            if ( CollUtil.isEmpty(list) ) {
                return new HttpResponseEntity<List<AQIDistributeDTO>>().resultIsNull(null);
            }
            // 2. 结果不为空时，远程调用AQI服务获取AQI等级/详情信息
            HttpResponseEntity<List<Aqi>> aqiResponse = aqiClient.selectAllAqi();
            if ( aqiResponse.getCode() == 200 ) {
                List<Aqi> aqiList = aqiClient.selectAllAqi().getData();
                list.forEach(item -> {
                    Aqi aqi = aqiList.stream().filter(a -> a.getAqiId().equals(item.getAqiId())).findFirst().orElse(null);
                    if ( aqi != null ) {
                        item.setColor(aqi.getColor());
                        item.setAqiLevel(aqi.getAqiLevel());
                        item.setAqiExplain(aqi.getAqiExplain());
                        double percent = ((double) item.getCount() / total) * 100;
                        item.setPercent(NumberUtil.roundStr(percent, 2));
                    }
                });
                return new HttpResponseEntity<List<AQIDistributeDTO>>().success(list);
            } else {
                return new HttpResponseEntity<List<AQIDistributeDTO>>().error(ResponseEnum.SERVICE_UNAVAILABLE);
            }
        } catch ( QueryException e ) {
            logger.error("远程调用AQI服务失败:{}", e.getMessage(), e);
            return new HttpResponseEntity<List<AQIDistributeDTO>>().error(ResponseEnum.SERVICE_UNAVAILABLE);
        } catch ( Exception e ) {
            logger.error("查询AQI指数等级分布统计失败:{}", e.getMessage(), e);
            throw new QueryException("查询AQI指数等级分布统计时发生异常", e);
        }
    }

    /**
     * 查询全国统计信息总览
     *
     * @return 全国统计信息总览
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<Map<String,StatisticsTotalDTO>> selectStatisticsSummary() {
        // 1. 异步调用获取完整的统计信息
        LocalDateTime now = LocalDateTime.now();
        CompletableFuture<StatisticsTotalDTO> nowTotalDTOFuture = CompletableFuture.supplyAsync(() -> statisticsMapper.selectStatisticsSummary(now));
        CompletableFuture<StatisticsTotalDTO> monthAgoTotalDTOFuture = CompletableFuture.supplyAsync(() -> statisticsMapper.selectStatisticsSummary(now.minusMonths(1)));
        CompletableFuture<HttpResponseEntity<Map<Object, Object>>> gridTotalFuture = CompletableFuture.supplyAsync(gridClient::selectGridTotal);

        try {
            // 2. 等待所有调用完成
            CompletableFuture.allOf(nowTotalDTOFuture, monthAgoTotalDTOFuture, gridTotalFuture).join();
            StatisticsTotalDTO nowTotalDTO = nowTotalDTOFuture.get();
            StatisticsTotalDTO monthAgoTotalDTO = monthAgoTotalDTOFuture.get();
            HttpResponseEntity<Map<Object, Object>> gridTotalResponse = gridTotalFuture.get();
            // 3. 当返回值正常时，填充网格信息
            if ( gridTotalResponse.getCode() == 200 && nowTotalDTO != null && monthAgoTotalDTO != null ) {
                Map<Object, Object> gridTotal = gridTotalResponse.getData();

                Integer province = (Integer) gridTotal.get("province");
                Integer city = (Integer) gridTotal.get("city");
                Integer town = (Integer) gridTotal.get("town");

                nowTotalDTO.setProvince(province);
                nowTotalDTO.setCity(city);
                nowTotalDTO.setTown(town);

                monthAgoTotalDTO.setProvince(province);
                monthAgoTotalDTO.setCity(city);
                monthAgoTotalDTO.setTown(town);

                logger.info("查询 本日 与 上月本日 的全国统计信息总览成功");
                return new HttpResponseEntity<Map<String,StatisticsTotalDTO>>().success(Map.of("now", nowTotalDTO, "monthAgo", monthAgoTotalDTO));
            }
            logger.warn("远程调用查询全国统计信息总览时，获取网格信息失败");
            return new HttpResponseEntity<Map<String,StatisticsTotalDTO>>().resultIsNull(null);
        } catch ( InterruptedException | ExecutionException e ) {
            logger.error("异步调用查询全国统计信息总览失败:{}", e.getMessage(), e);
            throw new QueryException("异步调用查询全国统计信息总览时发生异常", e);
        }
    }

    /**
     * 根据统计信息远程调用填充StatisticsDTO
     */
    private StatisticsDTO fillStatisticsDTO(Statistics statistics) throws QueryException {
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
        CompletableFuture.allOf(userFuture, gmFuture, gridFuture).join();

        try {
            HttpResponseEntity<User> userResponse = userFuture.get();
            HttpResponseEntity<User> gmResponse = gmFuture.get();
            HttpResponseEntity<Grid> gridResponse = gridFuture.get();

            if ( userResponse.getCode() != 200 || gmResponse.getCode() != 200 || gridResponse.getCode() != 200 ) {
                logger.warn("通过远程调用,填充统计信息: {} 的DTO失败", statistics.getStatisticsId());
                return null;
            }

            StatisticsDTO statisticsDTO = new StatisticsDTO(statistics);
            statisticsDTO.fillUserInfo(userResponse.getData(), gmResponse.getData());
            statisticsDTO.fillGridInfo(gridResponse.getData());
            return statisticsDTO;
        } catch ( InterruptedException | ExecutionException e ) {
            logger.error("填充统计信息: {} 的DTO时发生异常", statistics.getStatisticsId(), e);
            throw new QueryException("填充统计信息DTO时发生异常", e);
        }
    }

    private List<StatisticsDTO> fillStatisticsDTO(List<Statistics> statisticsList) {
        List<String> gmUserIds = statisticsList.stream().map(Statistics::getGmUserId).toList();
        List<String> userIds = statisticsList.stream().map(Statistics::getUserId).toList();
        List<String> townCodes = statisticsList.stream().map(Statistics::getTownCode).toList();

        CompletableFuture<HttpResponseEntity<List<User>>> gmFuture = CompletableFuture.supplyAsync(() -> userClient.selectBatchUser(gmUserIds));
        CompletableFuture<HttpResponseEntity<List<User>>> userFuture = CompletableFuture.supplyAsync(() -> userClient.selectBatchUser(userIds));
        CompletableFuture<HttpResponseEntity<List<Grid>>> gridFuture = CompletableFuture.supplyAsync(() -> gridClient.selectGridByMultipleTownCodes(townCodes));

        // 等待异步调用完成
        CompletableFuture.allOf(gmFuture, userFuture, gridFuture).join();
        try {
            HttpResponseEntity<List<User>> gmResponse = gmFuture.get();
            HttpResponseEntity<List<User>> userResponse = userFuture.get();
            HttpResponseEntity<List<Grid>> gridResponse = gridFuture.get();

            if ( gmResponse.getCode()!= 200 || gridResponse.getCode() != 200 || userResponse.getCode()!=200 ) {
                return Collections.emptyList();
            } else {
                Map<String, User> gmsById = gmResponse.getData().stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
                Map<String, User> usersById = userResponse.getData().stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
                Map<String, Grid> gridsByTownCode = gridResponse.getData().stream().collect(Collectors.toMap(Grid::getTownCode, Function.identity()));
                List<StatisticsDTO> statisticsDTOS = new ArrayList<>();
                for ( Statistics s : statisticsList ) {
                    StatisticsDTO statisticsDTO = new StatisticsDTO(s);
                    statisticsDTO.fillUserInfo(usersById.get(s.getUserId()), gmsById.get(s.getGmUserId()));
                    statisticsDTO.fillGridInfo(gridsByTownCode.get(s.getTownCode()));
                    statisticsDTOS.add(statisticsDTO);
                }
                logger.info("批量填充检测信息DTO成功");
                return statisticsDTOS;
            }
        } catch ( ExecutionException | InterruptedException e ) {
            logger.error("批量填充检测信息DTO时，异步远程调用发生异常", e);
            throw new QueryException("批量填充检测信息DTO时，异步远程调用发生异常", e);
        }
    }
}
