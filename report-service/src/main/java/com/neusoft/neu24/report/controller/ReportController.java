package com.neusoft.neu24.report.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.ReportDTO;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.exceptions.QueryException;
import com.neusoft.neu24.exceptions.SaveException;
import com.neusoft.neu24.exceptions.UpdateException;
import com.neusoft.neu24.report.service.IReportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <b>反馈信息前端控制器</b>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    /**
     * 反馈信息业务层接口
     */
    @Resource
    IReportService reportService;

    /**
     * 新建反馈信息
     *
     * @param map 反馈信息
     * @return 新建的反馈信息
     */
    @PostMapping(value = "/add", headers = "Accept=application/json")
    public HttpResponseEntity<Report> addReport(@RequestBody Map<String, Object> map) {
        try {
            // 1. 封装待保存的反馈信息
            Report report = BeanUtil.fillBeanWithMap(map, new Report(), false);
            // 2. 新建反馈信息
            return reportService.addReport(report);
        } catch ( SaveException e ) {
            // 3. 保存异常处理
            logger.error("新建反馈信息时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Report>().serverError(null);
        }
    }

    /**
     * 为反馈指派网格员
     *
     * @param map 反馈ID和网格员ID信息
     * @return 是否指派成功
     */
    @PutMapping(value = "/assign", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> reportAssign(@RequestBody Map<String, Object> map) {

        // 1. 获取用户传入的参数
        String reportId = (String) map.get("reportId");
        String gmUserId = (String) map.get("gmUserId");

        try {
            // 2. 指派网格员
            return reportService.assignGridManager(reportId, gmUserId);
        } catch ( UpdateException e ) {
            // 3. 指派异常处理
            logger.error("指派网格员时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }

    /**
     * 根据反馈ID查询反馈信息
     *
     * @param reportId 反馈ID
     * @return 查询结果
     */
    @GetMapping(value = "/select")
    public HttpResponseEntity<ReportDTO> selectReportById(@RequestParam("reportId") String reportId) {
        try {
            // 1. 根据反馈ID查询反馈信息
            return reportService.selectReportById(reportId);
        } catch ( QueryException e ) {
            // 2. 查询异常处理
            logger.info("根据反馈ID查询反馈信息时发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<ReportDTO>().serverError(null);
        }
    }

    /**
     * 根据条件分页查询反馈信息
     *
     * @param map     查询条件(为null时查询全部)
     * @param current 当前页
     * @param size    每页数据条数
     * @param aqiList 预估AQI等级多选条件
     * @return 分页查询结果
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<ReportDTO>> selectReportByPage(@RequestBody(required = false) Map<String, Object> map,
                                                                   @RequestParam("current") long current,
                                                                   @RequestParam("size") long size,
                                                                   @RequestParam(value = "aqiList", required = false) List<Integer> aqiList) {
        try {
            // 1. 封装查询条件
            Report report = null;
            if ( map != null && !map.isEmpty() ) {
                logger.info("Report分页查询条件: {}", map);
                report = BeanUtil.fillBeanWithMap(map, new Report(), false);
            }
            if ( aqiList != null && !aqiList.isEmpty() ) {
                logger.info("Report预估AQI等级多选条件: {}", aqiList);
            }
            // 2. 分页查询
            return reportService.selectReportByPage(report, current, size, aqiList);
        } catch ( QueryException e ) {
            // 3. 查询异常处理
            logger.error("条件分页查询反馈信息发生异常: {}", e.getMessage(), e);
            return new HttpResponseEntity<IPage<ReportDTO>>().serverError(null);
        }
    }

    /**
     * 更新反馈信息状态
     *
     * @param reportId 反馈ID
     * @param state    当前状态
     * @return 更新结果
     */
    @PostMapping("/state")
    public HttpResponseEntity<Boolean> setReportState(@RequestParam("reportId") String reportId, @RequestParam("state") Integer state) {
        try {
            // 1. 更新反馈信息状态
            return reportService.setReportState(reportId, state);
        } catch ( UpdateException e ) {
            // 2. 更新异常处理
            logger.error("更新反馈信息: {} 状态时发生异常: {}", reportId, e.getMessage(), e);
            return new HttpResponseEntity<Boolean>().serverError(null);
        }
    }
}

