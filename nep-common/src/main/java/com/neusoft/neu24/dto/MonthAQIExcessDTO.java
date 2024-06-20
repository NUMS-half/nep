package com.neusoft.neu24.dto;

import lombok.Data;

/**
 * 按月AQI超标统计信息类
 */
@Data
public class MonthAQIExcessDTO {

    private String month;

    private Integer excessCount;

    private String provinceCode;

    private String provinceName;
}
