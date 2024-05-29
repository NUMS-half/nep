package com.neusoft.neu24.report.controller;


import com.neusoft.neu24.entity.HttpResponseEntity;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.entity.User;
import com.neusoft.neu24.report.client.UserClient;
import com.neusoft.neu24.report.service.IReportService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 前端控制器
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


    @PutMapping(value = "/assign", headers = "Accept=application/json")
    public HttpResponseEntity<Boolean> reportAssign(@RequestBody Map<String,Object> map) {
        String reportId = (String) map.get("reportId");
        String gmUserId = (String) map.get("gmUserId");

        reportService.setGridManager(reportId, gmUserId);
        System.out.println("reportId: " + reportId + " gmUserId: " + gmUserId);
        return null;
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
                    report.setProvinceId((String) value);
                    break;
                case "cityId":
                    report.setCityId((String) value);
                    break;
                case "townId":
                    report.setTownId((String) value);
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

