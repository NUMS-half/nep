package com.neusoft.neu24.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Grid implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网格区域的省ID
     */
    private Integer provinceId;

    /**
     * 网格区域的市ID
     */
    private Integer cityId;

    /**
     * 网格区域的区/县ID
     */
    private Integer townId;

    /**
     * 网格区域的省名称
     */
    private String provinceName;

    /**
     * 网格区域的市名称
     */
    private String cityName;

    /**
     * 网格区域的区/县名称
     */
    private String townName;

    /**
     * 网格区域的省编码
     */
    private String provinceCode;

    /**
     * 网格区域的市编码
     */
    private String cityCode;

    /**
     * 网格区域的区/县编码
     */
    private String townCode;

}
