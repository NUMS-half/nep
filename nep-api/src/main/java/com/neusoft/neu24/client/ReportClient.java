package com.neusoft.neu24.client;

import com.neusoft.neu24.dto.ReportDTO;
import com.neusoft.neu24.entity.HttpResponseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("report-service")
public interface ReportClient {

    /**
     * 更新反馈信息状态
     * @param reportId 反馈ID
     * @param state 当前状态
     * @return 更新结果
     */
    @PostMapping("/report/state")
    HttpResponseEntity<Boolean> setReportState(@RequestParam("reportId") String reportId , @RequestParam("state") Integer state);

    @GetMapping(value = "/report/select/{reportId}")
    HttpResponseEntity<ReportDTO> selectReportById(@PathVariable String reportId);
}
