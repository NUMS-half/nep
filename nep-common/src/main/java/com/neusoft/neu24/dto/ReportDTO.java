package com.neusoft.neu24.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.Report;
import com.neusoft.neu24.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ReportDTO {

    // 反馈信息ID
    private String reportId;

    // 反馈者ID
    private String userId;

    // 反馈者账号
    private String username;

    // 反馈者姓名
    private String realName;

    // 反馈者电话
    private String telephone;

    // 反馈省份代码
    private String provinceCode;

    // 反馈省份名称
    private String provinceName;

    // 反馈城市代码
    private String cityCode;

    // 反馈城市名称
    private String cityName;

    // 反馈区县代码
    private String townCode;

    // 反馈区县名称
    private String townName;

    // 反馈详细地址
    private String address;

    // 反馈预估AQI等级
    private Integer estimatedLevel;

    // 反馈内容
    private String information;

    // 反馈时间
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    // 网格员ID
    private String gmUserId;

    // 指派时间
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignTime;

    // 反馈状态
    private Integer state;

    public ReportDTO(Report report) {
        this.reportId = report.getReportId();
        this.userId = report.getUserId();
        this.provinceCode = report.getProvinceCode();
        this.cityCode = report.getCityCode();
        this.townCode = report.getTownCode();
        this.address = report.getAddress();
        this.estimatedLevel = report.getEstimatedLevel();
        this.information = report.getInformation();
        this.reportTime = report.getReportTime();
        this.gmUserId = report.getGmUserId();
        this.assignTime = report.getAssignTime();
        this.state = report.getState();
    }

    public void fillUserInfo(User user) {
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.telephone = user.getTelephone();
    }

    public void fillGridInfo(Grid grid) {
        this.provinceName = grid.getProvinceName();
        this.cityName = grid.getCityName();
        this.townName = grid.getTownName();
    }
}
