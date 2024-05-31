package com.neusoft.neu24.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
public interface IReportService extends IService<Report> {

    /**
     * 新建公众监督员的反馈
     * @param report 反馈信息
     * @return 新建的反馈信息
     */
    HttpResponseEntity<Report> addReport(Report report);

    /**
     * 根据ID更新反馈信息
     * @param report 更新的反馈内容
     * @return 更新是否成功
     */
    HttpResponseEntity<Boolean> updateReport(Report report);

    HttpResponseEntity<Boolean> assignGridManager(String reportId, String gridManagerId);

    /**
     * 根据ID查询反馈信息
     * @param reportId 查询目标ID
     * @return 查询结果
     */
    HttpResponseEntity<Report> selectReportById(String reportId);

    /**
     * 条件分页查询反馈信息
     *
     * @param report 查询条件
     * @param current 当前页
     * @param size 每页数据条数
     * @return 分页查询结果
     */
    HttpResponseEntity<IPage<Report>> selectReportByPage(Report report, long current, long size);
}
