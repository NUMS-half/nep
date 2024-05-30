package com.neusoft.neu24.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.neusoft.neu24.client.UserClient;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.report.mapper.ReportMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neusoft.neu24.report.service.IReportService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 服务实现类
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
        return null;
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
     * @param reportId
     * @param userId
     * @return
     */
    @Override
    public HttpResponseEntity<Boolean> setGridManager(String reportId, String userId) {

        Map<String, Object> map = Map.of("userId", userId);
        User user = userClient.selectUser(map).getData();
        System.out.println("user: " + user);
        return null;
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
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();

        // 使用 HashMap 存储属性值，以确保类型正确
        Map<String, Object> params = new HashMap<>();
        params.put("report_id", report.getReportId());
        params.put("user_id", report.getUserId());
        params.put("province_id", report.getProvinceId());
        params.put("city_id", report.getCityId());
        params.put("town_id", report.getTownId());
        params.put("address", report.getAddress());
        params.put("information", report.getInformation());
        params.put("estimated_level", report.getEstimatedLevel());
        params.put("report_time", report.getReportTime());
        params.put("gm_user_id", report.getGmUserId());
        params.put("assign_time", report.getAssignTime());
        params.put("state", report.getState());
        // 添加查询条件
        queryWrapper.allEq(params);

        IPage<Report> pages = getBaseMapper().selectPage(page, queryWrapper);
        return new HttpResponseEntity<IPage<Report>>().success(pages);
    }
}
