package com.neusoft.neu24.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Data
@TableName("grid_city")
public class GridCity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网格区域的城市ID
     */
    @TableId(value = "city_id")
    private int cityId;

    /**
     * 网格区域的城市名称
     */
    private String cityName;

    /**
     * 网格区域的市编码
     */
    private String cityCode;

    /**
     * 网格区域的市所属的省编码
     */
    private String provinceCode;


}
