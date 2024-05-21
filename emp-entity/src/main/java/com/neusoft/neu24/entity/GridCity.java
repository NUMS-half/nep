package com.neusoft.neu24.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
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
@TableName("grid_city")
public class GridCity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网格区域的城市ID
     */
    private String cityId;

    /**
     * 网格区域的城市名称
     */
    private String cityName;

    /**
     * 网格区域的市所属的省ID
     */
    private String provinceId;


}
