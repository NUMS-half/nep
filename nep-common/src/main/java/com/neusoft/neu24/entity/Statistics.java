package com.neusoft.neu24.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * <p>
 * 
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Data
public class Statistics implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * AQI数据检测统计信息UUID
     */
    @TableId(value = "statistics_id")
    private String statisticsId;

    /**
     * 反馈ID
     */
    private String reportId;

    /**
     * 检测所属省份ID
     */
    private Integer provinceId;

    /**
     * 检测所属城市ID
     */
    private Integer cityId;

    /**
     * 检测所属区/县ID
     */
    private Integer townId;

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
    private LocalDateTime confirmTime;

    /**
     * 负责的网格员ID
     */
    private String gmUserId;

    /**
     * 上报的公众监督员ID
     */
    private String userId;

    /**
     * 上报的问题信息
     */
    private String information;

    /**
     * 备注
     */
    private String remarks;


}
