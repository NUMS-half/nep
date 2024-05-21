package com.neusoft.neu24.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Getter
@Setter
public class Aqi implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AQI级别编号(1-6级)
     */
    @TableId(value = "aqi_id", type = IdType.AUTO)
    private Integer aqiId;

    /**
     * AQI中文级别编号
     */
    private String aqiLevel;

    /**
     * AQI级别详细描述
     */
    private String aqiExplain;

    /**
     * AQI对应的颜色描述
     */
    private String color;

    /**
     * 该级别AQI对健康的影响
     */
    private String healthImpact;

    /**
     * 应当采取的措施
     */
    private String actions;

    /**
     * 该级别下二氧化硫最低限额
     */
    private Integer so2Min;

    /**
     * 该级别下二氧化硫最高限额
     */
    private Integer so2Max;

    /**
     * 该级别下一氧化碳最低限额
     */
    private Integer coMin;

    /**
     * 该级别下一氧化碳最高限额
     */
    private Integer coMax;

    /**
     * 该级别下悬浮颗粒物最低限额
     */
    private Integer spmMin;

    /**
     * 该级别下悬浮颗粒物最高限额
     */
    private Integer spmMax;

    /**
     * 该级别下AQI最低限额
     */
    private Integer aqiValMin;

    /**
     * 该级别下AQI最高限额
     */
    private Integer aqiValMax;

    /**
     * 备注
     */
    private String remarks;


}
