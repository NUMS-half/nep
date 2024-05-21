package com.neusoft.neu24.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("grid_province")
public class GridProvince implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 网格区域的省ID
     */
    private String provinceId;

    /**
     * 网格区域的省名称
     */
    private String provinceName;


}
