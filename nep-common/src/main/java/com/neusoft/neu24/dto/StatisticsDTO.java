package com.neusoft.neu24.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.neusoft.neu24.entity.Grid;
import com.neusoft.neu24.entity.Statistics;
import com.neusoft.neu24.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatisticsDTO {


    /**
     * AQI数据检测统计信息UUID
     */
    private String statisticsId;

    /**
     * 反馈ID
     */
    private String reportId;

    /**
     * 检测所属省份编码
     */
    private String provinceCode;

    /**
     * 检测所属省名称
     */
    private String provinceName;

    /**
     * 检测所属城市编码
     */
    private String cityCode;

    /**
     * 检测所属城市名称
     */
    private String cityName;

    /**
     * 检测所属区/县编码
     */
    private String townCode;

    /**
     * 检测所属区/县名称
     */
    private String townName;

    /**
     * 检测详细地址
     */
    private String address;

    /**
     * 实测二氧化硫浓度
     */
    private Integer so2Value;

    /**
     * 实测二氧化硫等级（1-6级）
     */
    private Integer so2Level;

    /**
     * 实测一氧化碳浓度
     */
    private Integer coValue;

    /**
     * 实测一氧化碳等级（1-6级）
     */
    private Integer coLevel;

    /**
     * 实测悬浮颗粒物浓度
     */
    private Integer spmValue;

    /**
     * 实测悬浮颗粒物等级（1-6级）
     */
    private Integer spmLevel;

    /**
     * AQI级别编号(1-6级)
     */
    private Integer aqiId;

    /**
     * 检测确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;

    /**
     * 负责的网格员ID
     */
    private String gmUserId;

    /**
     * 负责的网格员姓名
     */
    private String gmRealName;

    /**
     * 上报的公众监督员ID
     */
    private String userId;

    /**
     * 上报的公众监督员姓名
     */
    private String realName;

    /**
     * 上报的问题信息
     */
    private String information;

    /**
     * 备注
     */
    private String remarks;

    public StatisticsDTO(Statistics statistics) {
        this.statisticsId = statistics.getStatisticsId();
        this.reportId = statistics.getReportId();
        this.provinceCode = statistics.getProvinceCode();
        this.cityCode = statistics.getCityCode();
        this.townCode = statistics.getTownCode();
        this.address = statistics.getAddress();
        this.so2Value = statistics.getSo2Value();
        this.so2Level = statistics.getSo2Level();
        this.coValue = statistics.getCoValue();
        this.coLevel = statistics.getCoLevel();
        this.spmValue = statistics.getSpmValue();
        this.spmLevel = statistics.getSpmLevel();
        this.aqiId = statistics.getAqiId();
        this.confirmTime = statistics.getConfirmTime();
        this.gmUserId = statistics.getGmUserId();
        this.userId = statistics.getUserId();
        this.information = statistics.getInformation();
        this.remarks = statistics.getRemarks();
    }

    public void fillUserInfo(User user, User gmUser) {
        this.realName = user.getRealName();
        this.gmRealName = gmUser.getRealName();
    }

    public void fillGridInfo(Grid grid) {
        this.provinceName = grid.getProvinceName();
        this.cityName = grid.getCityName();
        this.townName = grid.getTownName();
    }
}
