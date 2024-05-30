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
@TableName("grid_province")
public class GridProvince implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网格区域的省ID
     */
    @TableId(value = "province_id")
    private int provinceId;

    /**
     * 网格区域的省名称
     */
    private String provinceName;

    /**
     * 网格区域的省编码
     */
    private String provinceCode;


}
