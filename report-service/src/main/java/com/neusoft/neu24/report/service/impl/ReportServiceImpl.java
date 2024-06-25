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
import com.neusoft.neu24.report.mapper.ReportMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.report.service.IReportService;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * <p>
 * 反馈信息服务实现类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Resource
    private ReportMapper reportMapper;

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 用户服务客户端(由动态代理注入)
     */
    private final UserClient userClient;

    /**
     * 网格服务客户端
     */
    private final GridClient gridClient;

    /**
     * 新建公众监督员的反馈
     *
     * @param report 反馈信息
     * @return 新建的反馈信息
     */
    @Override
    public HttpResponseEntity<Report> addReport(Report report) {
        // 1. 检查反馈信息中的区/县编码是否为空
        if ( StringUtil.isNullOrEmpty(report.getTownCode()) ) {
            return new HttpResponseEntity<Report>().fail(ResponseEnum.REGION_INVALID);
        }
        // 2. 远程调用检查该编号的区/县是否存在
        if ( gridClient.selectGridByTownCode(report.getTownCode()).getData() == null ) {
            return new HttpResponseEntity<Report>().fail(ResponseEnum.REGION_INVALID);
        }
        // 3. 检查预估等级是否合法
        if ( report.getEstimatedLevel() == null || report.getEstimatedLevel() < 1 || report.getEstimatedLevel() > 6 ) {
            return new HttpResponseEntity<Report>().fail(ResponseEnum.REPORT_FAIL_ESTIMATE_INVALID);
        }
        // 4. 插入反馈信息到数据库
        try {
            if ( reportMapper.insert(report) > 0 ) {
                return new HttpResponseEntity<Report>().success(report);
            } else {
                return new HttpResponseEntity<Report>().fail(ResponseEnum.ADD_FAIL);
            }
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Report>().fail(ResponseEnum.ADD_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Report>().serverError(null);
        }
    }

    /**
     * 根据ID更新反馈信息
     *
     * @param report 更新的反馈内容
     * @return 更新是否成功
     */
    @Override
    public HttpResponseEntity<Boolean> updateReport(Report report) {
        try {
            return reportMapper.updateById(report) != 0 ?
                    new HttpResponseEntity<Boolean>().success(null) :
                    new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 指派网格员
     *
     * @param reportId      反馈信息ID
     * @param gridManagerId 网格员ID
     * @return 是否指派成功
     */
    @Override
    public HttpResponseEntity<Boolean> assignGridManager(String reportId, String gridManagerId) {
        // 1. 构建查询条件，远程调用查询网格员信息
        Map<String, Object> map = Map.of(
                "userId", gridManagerId, // 被指派的用户ID
                "roleId", 2,             // 被指派的角色ID
                "gmStatus", 0);          // 网格员状态为空闲
        try {
            HttpResponseEntity<User> gridManager = userClient.selectUser(map);
            if ( gridManager.getCode() != 200 ) {
                // 2. 网格员不存在，指派失败
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.ASSIGN_FAIL_NO_GM, false);
            }

            // 3. 查询已经存在的反馈信息
            Report report = reportMapper.selectById(reportId);
            // 如果反馈信息不存在或已经指派，则指派失败
            if ( report == null || report.getState() != 0 ) {
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.ASSIGN_FAIL_HAS_ASSIGNED, false);
            }
            // 设置指派信息：指派网格员ID、指派时间、状态(1: 已指派)
            report.setGmUserId(gridManagerId);
            report.setAssignTime(LocalDateTimeUtil.now());
            report.setState(1);

            // 4. 更新反馈上报信息，进行指派
            if ( reportMapper.updateById(report) != 0 ) {
                // 5. 指派成功，则发送消息到消息队列
                // 发送到用户消息通知队列，通知网格员有新的反馈信息需要处理
                rabbitTemplate.convertAndSend("user.exchange", "notification." + gridManagerId, report);
                // 发送消息到指派成功队列，通知user-service更新网格员工作状态为 1:指派工作中
                rabbitTemplate.convertAndSend("user.exchange", "assign.success", gridManagerId);
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                return new HttpResponseEntity<Boolean>().fail(ResponseEnum.ASSIGN_FAIL_UPDATE_FAIL, false);
            }
        } catch ( Exception e ) {
            logger.error("指派网格员时发生异常: {}", e.getMessage());
            return new HttpResponseEntity<Boolean>().serverError(null);
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
    public HttpResponseEntity<ReportDTO> selectReportById(String reportId) {
        if ( reportId == null ) {
            return new HttpResponseEntity<ReportDTO>().resultIsNull(null);
        } else {
            QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("report_id", reportId);
            Report report = reportMapper.selectOne(queryWrapper);
            if ( report == null ) {
                return new HttpResponseEntity<ReportDTO>().resultIsNull(null);
            } else {
                ReportDTO reportDTO = fillReportDTO(report);
                return reportDTO == null ?
                        new HttpResponseEntity<ReportDTO>().resultIsNull(new ReportDTO(report)) :
                        new HttpResponseEntity<ReportDTO>().success(reportDTO);
            }
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
    public HttpResponseEntity<IPage<ReportDTO>> selectReportByPage(Report report, long current, long size) {
        IPage<Report> page = new Page<>(current, size);
        IPage<Report> pages;
        LambdaQueryWrapper<Report> queryWrapper = new LambdaQueryWrapper<>();
        if ( report != null ) {
            queryWrapper.eq(report.getUserId() != null, Report::getUserId, report.getUserId())
                    .eq(report.getProvinceCode() != null, Report::getProvinceCode, report.getProvinceCode())
                    .eq(report.getCityCode() != null, Report::getCityCode, report.getCityCode())
                    .eq(report.getTownCode() != null, Report::getTownCode, report.getTownCode())
                    .eq(report.getAddress() != null, Report::getAddress, report.getAddress())
                    .eq(report.getInformation() != null, Report::getInformation, report.getInformation())
                    .eq(report.getEstimatedLevel() != null, Report::getEstimatedLevel, report.getEstimatedLevel())
                    .eq(report.getGmUserId() != null, Report::getGmUserId, report.getGmUserId())
                    .eq(report.getState() != null, Report::getState, report.getState());
            pages = getBaseMapper().selectPage(page, queryWrapper);
        } else {
            pages = getBaseMapper().selectPage(page, null);
        }
        if ( pages == null || pages.getTotal() == 0 ) {
            return new HttpResponseEntity<IPage<ReportDTO>>().resultIsNull(null);
        }
        IPage<ReportDTO> dtoPages = pages.convert(this::fillReportDTO);
        return new HttpResponseEntity<IPage<ReportDTO>>().success(dtoPages);
    }

    /**
     * 设置反馈信息状态
     *
     * @param reportId 反馈信息ID
     * @param state    状态
     * @return 是否设置成功
     */
    @Override
    public HttpResponseEntity<Boolean> setReportState(String reportId, Integer state) {
        // 检查状态是否合法
        if ( state == null || state < 0 || state > 2 ) {
            return new HttpResponseEntity<Boolean>().fail(ResponseEnum.STATE_INVALID);
        }
        try {
            Report report = reportMapper.selectById(reportId);
            if ( report == null ) {
                return new HttpResponseEntity<Boolean>().resultIsNull(null);
            }
            report.setState(state);
            reportMapper.updateState(report.getState(), report.getReportId());
//            int i = 1 / 0;
            return        new HttpResponseEntity<Boolean>().success(true);
//                    new HttpResponseEntity<Boolean>().fail(ResponseEnum.UPDATE_FAIL);
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 填充反馈信息的用户信息和网格信息
     *
     * @param report 反馈信息
     * @return 填充后的反馈信息
     */
    private ReportDTO fillReportDTO(Report report) {
        HttpResponseEntity<User> userResponse = userClient.selectUser(Map.of("userId", report.getUserId()));
        HttpResponseEntity<Grid> gridResponse = gridClient.selectGridByTownCode(report.getTownCode());
        if ( userResponse.getCode() != 200 || gridResponse.getCode() != 200 ) {
            return null;
        }
        ReportDTO reportDTO = new ReportDTO(report);
        reportDTO.fillUserInfo(userResponse.getData());
        reportDTO.fillGridInfo(gridResponse.getData());
        return reportDTO;
    }
}
