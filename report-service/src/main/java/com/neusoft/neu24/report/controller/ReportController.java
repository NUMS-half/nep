package com.neusoft.neu24.report.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.dto.ReportDTO;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.report.service.IReportService;
import com.neusoft.neu24.utils.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 反馈信息前端控制器
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Slf4j
@RestController
@RequestMapping("/report")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Resource
    IReportService reportService;

    /**
     * 新建反馈
     *
     * @param map 反馈信息
     * @return 新建的反馈信息
     */
    @PostMapping(value = "/add", headers = "Accept=application/json")
    public HttpResponseEntity<Report> addReport(@RequestBody Map<String, Object> map) {
        Report report = BeanUtil.fillBeanWithMap(map, new Report(), false);
        report.setReportTime(LocalDateTime.now());
        report.setState(0);
        return reportService.addReport(report);
    }

    /**
     * 为反馈指派网格员
     *
     * @param map 反馈ID和网格员ID信息
     * @return 是否指派成功
     */
    @PutMapping(value = "/assign", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> reportAssign(@RequestBody Map<String, Object> map) {

        // 获取参数
        String reportId = (String) map.get("reportId");
        String gmUserId = (String) map.get("gmUserId");

        // 指派网格员
        return reportService.assignGridManager(reportId, gmUserId);
    }

    /**
     * 根据反馈ID查询反馈信息
     *
     * @param reportId 反馈ID
     * @return 查询结果
     */
    @GetMapping(value = "/select/{reportId}")
    public HttpResponseEntity<ReportDTO> selectReportById(@PathVariable String reportId) {
        return reportService.selectReportById(reportId);
    }

    /**
     * 根据条件分页查询反馈信息
     *
     * @param map 查询条件(为null时查询全部)
     * @param current 当前页
     * @param size 每页数据条数
     * @return 分页查询结果
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<ReportDTO>> selectReportByPage(@RequestBody(required = false) Map<String, Object> map, @RequestParam("current") long current, @RequestParam("size") long size) {

        try {
            if ( map == null || map.isEmpty() ) {
                return reportService.selectReportByPage(null, current, size);
            } else {
                logger.info("Report分页查询条件: {}", map);
                Report report = BeanUtil.fillBeanWithMap(map,new Report(), false);
                return reportService.selectReportByPage(report, current, size);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<IPage<ReportDTO>>().serverError(null);
        }
    }

    /**
     * 更新反馈信息状态
     * @param reportId 反馈ID
     * @param state 当前状态
     * @return 更新结果
     */
    @PostMapping("/state")
    public HttpResponseEntity<Boolean> setReportState(@RequestParam("reportId") String reportId ,@RequestParam("state") Integer state) {
        return reportService.setReportState(reportId,state);
    }
}

