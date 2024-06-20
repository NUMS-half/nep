package com.neusoft.neu24.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

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
