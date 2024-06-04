package com.neusoft.neu24.report.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.report.service.IReportService;
import com.neusoft.neu24.utils.UserContext;
import jakarta.annotation.Resource;
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
@RestController
@RequestMapping("/report")
public class ReportController {

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
        Report report = mapToReport(map);
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

        // 设置网格员
        return reportService.assignGridManager(reportId, gmUserId);
    }

    /**
     * 根据反馈ID查询反馈信息
     *
     * @param reportId 反馈ID
     * @return 查询结果
     */
    @GetMapping(value = "/select/{reportId}")
    public HttpResponseEntity<Report> selectReportById(@PathVariable String reportId) {
        return reportService.selectReportById(reportId);
    }

    /**
     * 根据条件分页查询反馈信息
     *
     * @param map 查询条件
     * @param current 当前页
     * @param size 每页数据条数
     * @return 分页查询结果
     */
    @PostMapping(value = "/select/page", headers = "Accept=application/json")
    public HttpResponseEntity<IPage<Report>> selectReportByPage(@RequestBody(required = false) Map<String, Object> map, @RequestParam("current") long current, @RequestParam("size") long size) {

        try {
            if ( map == null ) {
                return reportService.selectReportByPage(null, current, size);
            } else {
                Report report = mapToReport(map);
                return reportService.selectReportByPage(report, current, size);
            }
        } catch ( Exception e ) {
            return new HttpResponseEntity<IPage<Report>>().serverError(null);
        }
    }

    private Report mapToReport(Map<String, Object> map) {
        Report report = new Report();
        map.forEach((key, value) -> {
            switch ( key ) {
                case "reportId":
                    report.setReportId((String) value);
                    break;
                case "userId":
                    report.setUserId((String) value);
                    break;
                case "provinceId":
                    report.setProvinceId((Integer) value);
                    break;
                case "cityId":
                    report.setCityId((Integer) value);
                    break;
                case "townId":
                    report.setTownId((Integer) value);
                    break;
                case "address":
                    report.setAddress((String) value);
                    break;
                case "information":
                    report.setInformation((String) value);
                    break;
                case "estimatedLevel":
                    report.setEstimatedLevel((Integer) value);
                    break;
                case "reportTime":
                    report.setReportTime((LocalDateTime) value);
                    break;
                case "gmUserId":
                    report.setGmUserId((String) value);
                    break;
                case "assignTime":
                    report.setAssignTime((LocalDateTime) value);
                    break;
                case "state":
                    report.setState((Integer) value);
                    break;
                default:
                    break;
            }
        });
        return report;
    }
}

