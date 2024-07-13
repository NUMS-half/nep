package com.neusoft.neu24.report.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.ReportDTO;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <b>反馈上报服务接口</b>
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

    /**
     * 指派网格员
     *
     * @param reportId      反馈信息ID
     * @param gridManagerId 网格员ID
     * @return 是否指派成功
     */
    HttpResponseEntity<Boolean> assignGridManager(String reportId, String gridManagerId);

    /**
     * 根据ID查询反馈信息
     * @param reportId 查询目标ID
     * @return 查询结果
     */
    HttpResponseEntity<ReportDTO> selectReportById(String reportId);

    /**
     * 条件分页查询反馈信息
     *
     * @param report 查询条件
     * @param current 当前页
     * @param size 每页数据条数
     * @return 分页查询结果
     */
    HttpResponseEntity<IPage<ReportDTO>> selectReportByPage(Report report, long current, long size, List<Integer> states);

    /**
     * 设置反馈信息状态
     *
     * @param reportId 反馈信息ID
     * @param state    状态
     * @return 是否设置成功
     */
    HttpResponseEntity<Boolean> setReportState(String reportId, Integer state);
}
