package com.neusoft.neu24.dto;

import lombok.Data;

/**
 * AQI指数等级分布统计信息类
 */
@Data
public class AQIDistributeDTO {

    private Integer aqiId;

    private String aqiLevel;

    private String aqiExplain;

    private String color;

    private String percent;

    private Integer count;

    private String provinceCode;

    private String provinceName;
}
