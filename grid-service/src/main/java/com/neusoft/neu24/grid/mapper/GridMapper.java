package com.neusoft.neu24.grid.mapper;

import com.neusoft.neu24.dto.StatisticsTotalDTO;
import com.neusoft.neu24.entity.Grid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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
     * 更新网格区/县信息
     * @param grid 网格区/县信息
     * @return 是否更新成功
     */
    boolean updateGridTown(Grid grid);

    /**
     * 根据区县编码获取完整的网格信息
     * @param townCode 区县编码
     * @return 完整的网格信息
     */
    List<Grid> selectGridByTownCode(@Param("townCode") String townCode);

    List<Grid> selectGridByMultipleTownCodes(@Param("townCodes") List<String> townCodes);

    List<Map<Object,Object>> selectCitiesMapByProvince(@Param("provinceCode") String provinceCode);

    List<Map<Object,Object>> selectProvinceMap();

    StatisticsTotalDTO selectGridTotal();
}
