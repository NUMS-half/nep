package com.neusoft.neu24.report.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.client.GridClient;
import com.neusoft.neu24.client.UserClient;
import com.neusoft.neu24.dto.ReportDTO;
import com.neusoft.neu24.entity.*;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.SaveException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.report.mapper.ReportMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.report.service.IReportService;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <b>
 * 反馈信息服务实现类
 * </b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    /**
     * 反馈信息Mapper
     */
    @Resource
    private ReportMapper reportMapper;

    /**
     * RabbitMQ操作模板
     */
    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * User-Service 客户端(由动态代理注入)
     */
    private final UserClient userClient;

    /**
     * Grid-Service 客户端
     */
    private final GridClient gridClient;

    /**
     * 新建公众监督员的反馈 (需要seata全局事务管理)
     *
     * @param report 待添加的反馈信息
     * @return 新建的反馈信息详情
     */
    @Override
    @GlobalTransactional
    public HttpResponseEntity<Report> addReport(Report report) throws SaveException {
        try {
            // 1. 检查反馈信息中的区/县编码是否为空
            if ( StringUtils.isBlank(report.getTownCode()) ) {
                return new HttpResponseEntity<Report>().fail(ResponseEnum.CONTENT_IS_NULL);
            }
            // 2. 远程调用检查该编号的区/县是否存在
            if ( gridClient.selectGridByTownCode(report.getTownCode()).getData() == null ) {
                return new HttpResponseEntity<Report>().fail(ResponseEnum.REGION_INVALID);
            }
            // 3. 检查预估等级是否合法
            if ( report.getEstimatedLevel() == null || report.getEstimatedLevel() < 1 || report.getEstimatedLevel() > 6 ) {
                return new HttpResponseEntity<Report>().fail(ResponseEnum.REPORT_FAIL_ESTIMATE_INVALID);
            }
            // 4. 保存反馈信息
            // 4.1 为反馈信息设置反馈时间与状态(0: 未指派)
            report.setReportTime(LocalDateTime.now());
            report.setState(0);
            // 4.2 插入反馈信息到数据库
            if ( reportMapper.insert(report) > 0 ) {
                logger.info("新建反馈信息成功: {}", report.getReportId());
                return new HttpResponseEntity<Report>().success(report);
            } else {
                return new HttpResponseEntity<Report>().fail(ResponseEnum.ADD_FAIL);
            }
        } catch ( DataAccessException e ) {
            // 5. 处理数据不符合数据库约束的异常
            logger.warn("数据不符合数据库约束，新建反馈信息失败: {}", e.getMessage(), e);
            throw new SaveException("数据不符合数据库约束，新建反馈信息失败", e);
        } catch ( Exception e ) {
            // 6. 处理其他异常
            logger.error("新建反馈信息时发生异常: {}", e.getMessage(), e);
            throw new SaveException("新建反馈信息时发生异常", e);
        }
    }

    /**
     * 根据ID更新反馈信息
     *
     * @param report 更新的反馈内容
     * @return 更新是否成功
     */
    @Override
    @GlobalTransactional
    public HttpResponseEntity<Boolean> updateReport(Report report) throws UpdateException {
        // 1. 检查要修改的反馈信息ID是否为空
        if ( StringUtils.isEmpty(report.getReportId()) ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 更新反馈信息
            return reportMapper.updateById(report) != 0 ?
                    new HttpResponseEntity<Boolean>().success(null) :
                    new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( DataAccessException e ) {
            // 3. 处理数据不符合数据库约束的异常
            logger.warn("数据不符合数据库约束，更新反馈信息失败: {}", e.getMessage(), e);
            throw new UpdateException("数据不符合数据库约束，更新反馈信息失败", e);
        } catch ( Exception e ) {
            // 4. 处理其他异常
            logger.error("更新反馈信息时发生异常: {}", e.getMessage(), e);
            throw new UpdateException("更新反馈信息时发生异常", e);
        }
    }

    /**
     * 为反馈信息指派网格员 (需要seata全局事务管理)
     *
     * @param reportId      反馈信息ID
     * @param gridManagerId 网格员ID
     * @return 是否指派成功
     */
    @Override
    @GlobalTransactional
    public HttpResponseEntity<Boolean> assignGridManager(String reportId, String gridManagerId) throws UpdateException {
        // 检查参数是否为空
        if ( StringUtils.isEmpty(reportId) || StringUtils.isEmpty(gridManagerId) ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 1. 查询反馈信息是否合法
            Report report = reportMapper.selectById(reportId);
            // 如果反馈信息不存在或已经指派，则指派失败
            if ( report == null || report.getState() != 0 ) {
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.ASSIGN_FAIL_HAS_ASSIGNED, false);
            }

            // 2. 查询网格员信息是否合法
            // 2.1 构建查询条件，远程调用查询网格员信息
            Map<String, Object> map = Map.of(
                    "userId", gridManagerId,  // 被指派的用户ID
                    "roleId", 2);             // 被指派的角色ID
            HttpResponseEntity<User> gmResponse = userClient.selectUser(map);
            // 2.2 网格员不存在，指派失败
            if ( gmResponse.getCode() != 200 ) {
                logger.warn("指派时，查询网格员: {} 信息失败", gridManagerId);
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.ASSIGN_FAIL_NO_GM, false);
            }

            // 3. 设置指派信息：指派网格员ID、指派时间、状态(1: 已指派)
            report.setGmUserId(gridManagerId);
            report.setAssignTime(LocalDateTimeUtil.now());
            report.setState(1);

            // 4. 更新反馈上报信息，进行指派
            reportMapper.updateById(report);
            // 5. 指派成功，更新网格员工作状态(同步)
            if ( userClient.updateGmState(gridManagerId, 1).getCode() != 200 ) {
                logger.error("指派时，更新网格员: {} 工作状态失败", gridManagerId);
                throw new UpdateException();
            }
//            // 5. 指派成功，更新网格员工作状态(异步): 发送消息到指派成功队列，通知user-service更新网格员工作状态为 1:指派工作中
//            rabbitTemplate.convertAndSend("user.exchange", "assign.success", gridManagerId);
            // 6. 发送到用户消息通知队列，通知网格员有新的反馈信息需要处理
            rabbitTemplate.convertAndSend("user.exchange", "notification." + gridManagerId, report);
            rabbitTemplate.convertAndSend("user.exchange", "notification." + report.getUserId(), report);
            logger.info("为反馈信息: {} 指派网格员: {} 成功", reportId, gridManagerId);
            return new HttpResponseEntity<Boolean>().success(true);
        } catch ( Exception e ) {
            // 7. 处理其他异常
            logger.error("指派网格员时发生异常: {}", e.getMessage());
            throw new UpdateException("指派网格员时发生异常", e);
        }
    }

    /**
     * 根据ID查询反馈信息
     *
     * @param reportId 查询目标ID
     * @return 查询结果
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<ReportDTO> selectReportById(String reportId) throws QueryException {
        // 1. 检查反馈信息ID是否为空
        if ( reportId == null ) {
            return new HttpResponseEntity<ReportDTO>().fail(ResponseEnum.CONTENT_IS_NULL);
        }
        try {
            // 2. 查询反馈信息
            QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("report_id", reportId);
            Report report = reportMapper.selectOne(queryWrapper);
            if ( report == null ) {
                return new HttpResponseEntity<ReportDTO>().resultIsNull(null);
            } else {
                // 3. 填充反馈信息的用户信息和网格信息
                ReportDTO reportDTO = fillReportDTO(report);
                return reportDTO == null ?
                        new HttpResponseEntity<ReportDTO>().resultIsNull(new ReportDTO(report)) :
                        new HttpResponseEntity<ReportDTO>().success(reportDTO);
            }
        } catch ( Exception e ) {
            // 4. 处理其他异常
            logger.error("根据ID: {} 查询反馈信息时发生异常", reportId, e);
            throw new QueryException("根据ID查询反馈信息时发生异常", e);
        }
    }

    /**
     * 条件分页查询反馈信息
     *
     * @param report  查询条件
     * @param current 当前页
     * @param size    每页数据条数
     * @return 分页查询结果
     */
    @Override
    @Transactional(readOnly = true)
    public HttpResponseEntity<IPage<ReportDTO>> selectReportByPage(Report report, long current, long size, List<Integer> aqiList) throws QueryException {
        try {
            // 1. 分页查询反馈信息
            IPage<Report> page = new Page<>(current, size);
            IPage<Report> pages;
            LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
            // 2. 设置查询条件
            if ( report != null ) {
                queryWrapper.eq(StringUtils.isNotBlank(report.getUserId()), Report::getUserId, report.getUserId())
                        .eq(StringUtils.isNotBlank(report.getProvinceCode()), Report::getProvinceCode, report.getProvinceCode())
                        .eq(StringUtils.isNotBlank(report.getCityCode()), Report::getCityCode, report.getCityCode())
                        .eq(StringUtils.isNotBlank(report.getTownCode()), Report::getTownCode, report.getTownCode())
                        .eq(StringUtils.isNotBlank(report.getAddress()), Report::getAddress, report.getAddress())
                        .eq(StringUtils.isNotBlank(report.getInformation()), Report::getInformation, report.getInformation())
//                        .eq(report.getEstimatedLevel() != null, Report::getEstimatedLevel, report.getEstimatedLevel())
                        .eq(StringUtils.isNotBlank(report.getGmUserId()), Report::getGmUserId, report.getGmUserId())
                        .eq(report.getState() != null, Report::getState, report.getState());

            }
            // 3. 不为空时，设置预估AQI等级的多选条件
            if ( aqiList != null && !aqiList.isEmpty() ) {
                queryWrapper.in(Report::getEstimatedLevel, aqiList);
            }
            // 4. 按照反馈时间降序排序返回查询结果
            queryWrapper.orderByDesc(Report::getReportTime);
            pages = getBaseMapper().selectPage(page, queryWrapper);
            if ( pages == null || pages.getTotal() == 0 ) {
                return new HttpResponseEntity<IPage<ReportDTO>>().resultIsNull(null);
            }
            // 5. 将用户信息和网格信息填充到反馈信息DTO中
            IPage<ReportDTO> dtoPages = new Page<>();
            dtoPages.setRecords(fillReportDTOList(pages.getRecords()));
            dtoPages.setTotal(pages.getTotal());
            dtoPages.setCurrent(pages.getCurrent());
            dtoPages.setSize(pages.getSize());
            return new HttpResponseEntity<IPage<ReportDTO>>().success(dtoPages);
        } catch ( Exception e ) {
            // 6. 处理其他异常
            logger.error("分页条件查询反馈信息时发生异常", e);
            throw new QueryException("分页条件查询反馈信息时发生异常", e);
        }
    }

    /**
     * 设置反馈信息状态
     *
     * @param reportId 反馈信息ID
     * @param state    目标状态
     * @return 是否设置成功
     */
    @Override
    public HttpResponseEntity<Boolean> setReportState(String reportId, Integer state) throws UpdateException {
        // 1. 检查状态是否合法
        if ( state == null || state < 0 || state > 2 ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.STATE_INVALID);
        }
        try {
            // 2. 校验要修改的反馈信息是否存在
            Report report = reportMapper.selectById(reportId);
            if ( report == null ) {
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.REPORT_NOT_EXIST);
            }
            // 3. 信息存在，设置状态
            if ( reportMapper.updateState(state, reportId) > 0 ) {
                // 4. 更新成功
                logger.info("成功更新反馈信息: {} 的状态为: {}", reportId, state);
                // 5. 发送消息到用户消息通知队列，通知公众监督员反馈信息状态已更新
                rabbitTemplate.convertAndSend("user.exchange", "notification." + report.getUserId(), report);
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                logger.warn("更新反馈信息: {} 状态失败", reportId);
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
            }
        } catch ( Exception e ) {
            // 6. 处理其他异常
            logger.error("更新反馈信息: {} 状态时发生异常", reportId, e);
            throw new UpdateException("更新反馈信息状态时发生异常", e);
        }
    }

    /**
     * 填充反馈信息的用户信息和网格信息
     *
     * @param report 反馈信息
     * @return 填充后的反馈信息
     */
    private ReportDTO fillReportDTO(Report report) throws QueryException {
        // 1. 异步调用查询用户信息和网格信息
        CompletableFuture<HttpResponseEntity<User>> userFuture = CompletableFuture.supplyAsync(() -> userClient.selectUser(Map.of("userId", report.getUserId())));
        CompletableFuture<HttpResponseEntity<Grid>> gridFuture = CompletableFuture.supplyAsync(() -> gridClient.selectGridByTownCode(report.getTownCode()));

        // 2. 等待异步调用完成，并获取结果
        CompletableFuture.allOf(userFuture, gridFuture).join();
        try {
            HttpResponseEntity<User> userResponse = userFuture.get();
            HttpResponseEntity<Grid> gridResponse = gridFuture.get();
            // 3. 两者有其一查询失败则返回空
            if ( userResponse.getCode() != 200 || gridResponse.getCode() != 200 ) {
                return null;
            } else {
                ReportDTO reportDTO = new ReportDTO(report);
                reportDTO.fillUserInfo(userResponse.getData());
                reportDTO.fillGridInfo(gridResponse.getData());
                logger.info("填充上报信息: {} DTO成功", report.getReportId());
                return reportDTO;
            }
        } catch ( ExecutionException | InterruptedException e ) {
            // 4. 处理异步调用异常
            logger.error("填充上报信息: {} DTO时，异步远程调用发生异常", report.getReportId(), e);
            throw new QueryException("填充上报信息DTO时，异步远程调用发生异常", e);
        }
    }

    /**
     * 批量填充反馈信息的用户信息和网格信息
     *
     * @param reports 反馈信息列表
     * @return 填充后的反馈信息列表
     */
    private List<ReportDTO> fillReportDTOList(List<Report> reports) {
        // 1. 获取所有要查询的用户ID列表和区/县编码列表
        List<String> userIds = reports.stream().map(Report::getUserId).toList();
        List<String> townCodes = reports.stream().map(Report::getTownCode).toList();

        // 2. 异步调用批量查询用户信息和网格信息
        CompletableFuture<HttpResponseEntity<List<User>>> userFuture = CompletableFuture.supplyAsync(() -> userClient.selectBatchUser(userIds));
        CompletableFuture<HttpResponseEntity<List<Grid>>> gridFuture = CompletableFuture.supplyAsync(() -> gridClient.selectGridByMultipleTownCodes(townCodes));

        // 3. 等待异步调用完成，并获取结果
        CompletableFuture.allOf(userFuture, gridFuture).join();
        try {
            HttpResponseEntity<List<User>> userResponse = userFuture.get();
            HttpResponseEntity<List<Grid>> gridResponse = gridFuture.get();
            // 4. 两者有其一查询失败则返回空
            if ( userResponse.getCode() != 200 || gridResponse.getCode() != 200 ) {
                return Collections.emptyList();
            } else {
                // 5. 将用户信息和网格信息转换为Map，提高填充效率
                Map<String, User> usersById = userResponse.getData().stream().collect(Collectors.toMap(User::getUserId, Function.identity()));
                Map<String, Grid> gridsByTownCode = gridResponse.getData().stream().collect(Collectors.toMap(Grid::getTownCode, Function.identity()));

                // 6. 填充反馈信息DTO列表
                List<ReportDTO> reportDTOs = new ArrayList<>();
                for ( Report report : reports ) {
                    ReportDTO reportDTO = new ReportDTO(report);
                    reportDTO.fillUserInfo(usersById.get(report.getUserId()));
                    reportDTO.fillGridInfo(gridsByTownCode.get(report.getTownCode()));
                    reportDTOs.add(reportDTO);
                }
                logger.info("批量填充上报信息DTO成功");
                return reportDTOs;
            }
        } catch ( ExecutionException | InterruptedException e ) {
            // 7. 处理异步调用异常
            logger.error("批量填充上报信息DTO时，异步远程调用发生异常", e);
            throw new QueryException("批量填充上报信息DTO时，异步远程调用发生异常", e);
        }
    }
}
