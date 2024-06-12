package com.neusoft.neu24.report.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.client.UserClient;
import com.neusoft.neu24.component.MQProducer;
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
    MQProducer<Report> mqProducer;

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 用户服务客户端(由动态代理注入)
     */
    private final UserClient userClient;

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

            // 4. 更新反馈信息
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
    public HttpResponseEntity<Report> selectReportById(String reportId) {
        if ( reportId == null ) {
            return new HttpResponseEntity<Report>().resultIsNull(null);
        } else {
            QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("report_id", reportId);
            Report result = reportMapper.selectOne(queryWrapper);
            if ( result == null ) {
                return new HttpResponseEntity<Report>().resultIsNull(null);
            } else {
                return new HttpResponseEntity<Report>().success(result);
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
    public HttpResponseEntity<IPage<Report>> selectReportByPage(Report report, long current, long size) {
        IPage<Report> page = new Page<>(current, size);
        IPage<Report> pages;
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        if ( report != null ) {
            // 使用 HashMap 存储属性值，以确保类型正确
            Map<String, Object> params = new HashMap<>();
            params.put("report_id", report.getReportId());
            params.put("user_id", report.getUserId());
            params.put("province_code", report.getProvinceCode());
            params.put("city_code", report.getCityCode());
            params.put("town_code", report.getTownCode());
            params.put("address", report.getAddress());
            params.put("information", report.getInformation());
            params.put("estimated_level", report.getEstimatedLevel());
            params.put("report_time", report.getReportTime());
            params.put("gm_user_id", report.getGmUserId());
            params.put("assign_time", report.getAssignTime());
            params.put("state", report.getState());
            // 添加查询条件
            queryWrapper.allEq(params);

            pages = getBaseMapper().selectPage(page, queryWrapper);
        } else {
            pages = getBaseMapper().selectPage(page, null);
        }
        return pages == null || pages.getTotal() == 0 ?
                new HttpResponseEntity<IPage<Report>>().resultIsNull(null) :
                new HttpResponseEntity<IPage<Report>>().success(pages);
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
}
