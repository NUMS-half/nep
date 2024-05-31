package com.neusoft.neu24.grid.mapper;

import com.neusoft.neu24.entity.Grid;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Mapper
public interface GridMapper {

    /**
     * 获取所有省份信息
     * @return 省份信息列表
     */
    List<Grid> getProvinces();

    /**
     * 根据省份编码获取所有城市信息
     * @param provinceCode 省份编码
     * @return 对应的城市信息列表
     */
    List<Grid> getCitiesByProvinceCode(String provinceCode);

    /**
     * 根据城市编码获取所有区县信息
     * @param cityCode 城市编码
     * @return 对应的区县信息列表
     */
    List<Grid> getTownsByCityCode(String cityCode);

    /**
     * 更新网格区/县信息
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    boolean updateGridTown(Grid grid);
}
