package com.neusoft.neu24.dto;

import lombok.Data;

/**
 * 省/市分项指标超标统计信息类
 */
@Data
public class ItemizedStatisticsDTO {

    private String provinceCode;

    private String provinceName;

    private String cityCode;

    private String cityName;

    private Integer so2ExcessCount;

    private Integer coExcessCount;

    private Integer spmExcessCount;

    private Integer aqiExcessCount;
}
