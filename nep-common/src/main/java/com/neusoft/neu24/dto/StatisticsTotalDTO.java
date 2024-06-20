package com.neusoft.neu24.dto;

import lombok.Data;

@Data
public class StatisticsTotalDTO {

    // 统计数据总数
    private Integer total;

    // 优/良记录总数
    private Integer goodCount;

    // 污染记录总数
    private Integer pollutionCount;

    // 省份总数(34)
    private Integer province;

    // 全国省份覆盖总数
    private Integer gridProvinceCount;

    // 省会城市覆盖总数
    private Integer gridCapitalCityCount;

    // 城市总数(342)
    private Integer city;

    // 全国城市覆盖总数
    private Integer gridCityCount;

    // 区/县总数(3142)
    private Integer town;
}
