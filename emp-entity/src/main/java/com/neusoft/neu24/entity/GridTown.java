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
@TableName("grid_town")
public class GridTown implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网格区域的区/县ID
     */
    private String townId;

    /**
     * 网格区域的区/县名称
     */
    private String townName;

    /**
     * 网格区域的区/县所属的城市ID
     */
    private String cityId;


}
