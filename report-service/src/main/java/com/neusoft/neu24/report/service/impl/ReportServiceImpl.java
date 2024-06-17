package com.neusoft.neu24.report.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.client.GridClient;
import com.neusoft.neu24.client.UserClient;
import com.neusoft.neu24.dto.ReportDTO;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.report.mapper.ReportMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.report.service.IReportService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 反馈信息服务实现类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

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
        try {
            if( reportMapper.insert(report) != 0 ) {
                return new HttpResponseEntity<Report>().success(report);
            } else {
                return new HttpResponseEntity<Report>().addFail(null);
            }
        } catch ( DataAccessException e ) {
            return new HttpResponseEntity<Report>().addFail(null);
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
                    HttpResponseEntity.UPDATE_FAIL;
        } catch ( DataAccessException e ) {
            return HttpResponseEntity.UPDATE_FAIL;
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

        Map<String, Object> map = Map.of("userId", gridManagerId);
        try {
            // 1. 查询网格员信息
            HttpResponseEntity<User> gridManager = userClient.selectUser(map);
            if ( gridManager.getCode() != 200 ) {
                // 2. 网格员不存在，指派失败
                return HttpResponseEntity.ASSIGN_FAIL;
            }

            // 3. 查询已经存在的反馈信息
            Report report = reportMapper.selectById(reportId);
            // 如果反馈信息不存在或已经指派，则指派失败
            if ( report == null || report.getState() != 0 ) {
                return HttpResponseEntity.ASSIGN_FAIL;
            }
            report.setGmUserId(gridManagerId);
            report.setAssignTime(LocalDateTimeUtil.now());
            report.setState(1);

            // 4. 更新反馈信息为已指派
            if ( reportMapper.updateById(report) != 0 ) {
                // 5. 指派成功，则发送消息到消息队列
                rabbitTemplate.convertAndSend("user.exchange", "notification." + gridManagerId, report);
                return new HttpResponseEntity<Boolean>().success(true);
            } else {
                return HttpResponseEntity.ASSIGN_FAIL;
            }
        } catch ( Exception e ) {
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
            return HttpResponseEntity.STATE_INVALID;
        }
        try {
            Report report = reportMapper.selectById(reportId);
            if ( report == null ) {
                return new HttpResponseEntity<Boolean>().resultIsNull(null);
            }
            report.setState(state);
            return reportMapper.updateState(report.getState(), report.getReportId()) != 0 ?
                    new HttpResponseEntity<Boolean>().success(true) :
                    HttpResponseEntity.UPDATE_FAIL;
        } catch ( Exception e ) {
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 填充反馈信息的用户信息和网格信息
     * @param report 反馈信息
     * @return 填充后的反馈信息
     */
    private ReportDTO fillReportDTO(Report report) {
        HttpResponseEntity<User> userResponse = userClient.selectUser(Map.of("userId",report.getUserId()));
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
